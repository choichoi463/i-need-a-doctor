package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.example.browser.BrowserProvider;
import org.example.utils.ConfigReader;
import org.example.utils.MailReader;

import java.time.Duration;

import static org.example.utils.ConfigReader.*;

@Log
public class Luxmed {

    static boolean isRegistrationDone = false;

    public static void main(String[] args) {

        //setting up retry mechanism
        int MAX_RETRY_COUNT = getMaxRetryNumber(); //250h of total time run.
        int RETRY_INTERVAL_MINUTES = getRetryIntervalMinutes();
        for (int i = 0; i <= MAX_RETRY_COUNT; i++) {
            log.info("Starting execution #" + i);
            if (isRegistrationDone) {
                log.info("Loop ended. Registration is true.");
                break;
            }
            if (!isRegistrationDone) {
                endokrinologRegistration();
            }
            try {
                Thread.sleep(Duration.ofMinutes(RETRY_INTERVAL_MINUTES));
            } catch (InterruptedException e) {
                log.severe("Thread wait has failed! Please check why. Stopping the app.");
                throw new RuntimeException(e);
            }
        }
    }


    public static void endokrinologRegistration() {
        try (BrowserProvider browserProvider = new BrowserProvider()) {
            Browser browser = browserProvider.getBrowser();

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            //login part
            log.info("login part");
            page.navigate("https://portalpacjenta.luxmed.pl/PatientPortal/NewPortal/Page/Account/Login?returnUrl=%2FPage%2FDashboard");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).fill(ConfigReader.getLuxmedUsername());
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).press("Tab");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz hasło")).fill(ConfigReader.getLuxmedPassword());
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Zaloguj się")).click();

            //verification code via email
            //check initial message amount
            MailReader mailReader = new MailReader();
            Store store = null;
            try {
                store = mailReader.establishConnection();
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
            int initialMessageCount = mailReader.getMessageAmount(store);

            //ui verification
            log.info("verification part");
            page.getByText("Kontynuuj bez dodawania").click();
            page.getByText("Dalej").click();

            //wait till mail is received
            try {
                mailReader.waitTillNewMailReceived(store, initialMessageCount);
            } catch (Exception e) {
                log.warning("Warning! no emails were received for 3 minutes. Initiating a longer sleep timeout");
                int RETRY_INTERVAL_NO_MAIL_MINUTES = getNoEmailSleepInterval();
                try {
                    Thread.sleep(Duration.ofMinutes(RETRY_INTERVAL_NO_MAIL_MINUTES));
                    mailReader.waitTillNewMailReceived(store, initialMessageCount);
                } catch (Exception noNewEmails) {
                    log.severe("No emails received at all after " + RETRY_INTERVAL_NO_MAIL_MINUTES + " minutes! Please check if there any new notifications from luxmed manually.");
                }
            }

            //code mail extraction
            String code;
            try {
                log.info("If it works here you will see the amount of emails " + store.getFolder("Inbox").getMessageCount());
                Message messageWithCode = mailReader.findLastMessage(store, "noreply@info.luxmed.pl");
                code = mailReader.parseEmailGetCode(messageWithCode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            page.locator("#mfaEmailCodeInput").type(code);
            page.getByText("Potwierdź").last().click();

            //part with anketa
            //TODO if present
            try {
                Locator anketa = page.locator("iframe[title=\"Zaproszenie do ankiety\"]").contentFrame().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Przypomnij mi później"));
                if (anketa.isVisible()) {
                    log.info("closing anketa");
                    anketa.click();
                }
            } catch (Exception e) {
            }

            //part with selecting new visit to endokrinolog
            log.info("choosing a visit type");
            final String VISIT_TYPE_SEARCH_PARAM = "Konsultacja endokrynologiczna";
            final String VISIT_SHORT_NAME = "endo";
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Umów")).click();
            page.locator("input[name^='fake-attribute']").filter().all().get(1).click();
            page.locator("input[name^='fake-attribute']").filter().all().get(1).fill(VISIT_SHORT_NAME);
            page.getByText(VISIT_TYPE_SEARCH_PARAM, new Page.GetByTextOptions().setExact(true)).first().click();

            //part with yes/no options if visible
            if (page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").isVisible()) {
                log.info("various visit questions selection");
                page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
                page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
                page.locator("label").filter(new Locator.FilterOptions().setHasText("Tak")).locator("label").click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
                page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
            }

            //part with doctor name
            log.info("filtering by a doctor name");
            final String DOCTOR_NAME = "Ilona Minkiewicz";
            final String DOCTOR_SHORT_INDICATOR = "ilo";
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wybrano 1 z 5")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wybrano 1 z 5")).fill(DOCTOR_NAME);
//            page.getByText(DOCTOR_NAME).first().click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();

            //list of all found terms
            //if terms have something in the list
            log.info("looking through found available results");
            if (page.locator("css=app-term").count() > 0) {
                //clicks the last reservation slot available (can have multiple days in the list, ~30+ reservation slots)
                log.info("selecting the last available reservation slot");
                page.locator("div.time").last().hover();
                page.locator("css=div.reserve-button>button").last().click();
                // <button _ngcontent-ng-c1959152815="" class="btn btn-primary">Rezerwuj termin</button>
                // all buttons to reserve
                //$$("div.reserve-button>button")

                //TODO add check for too early time?
                // time divs looks like:
                // <div _ngcontent-ng-c1959152815="" class="time">08:45</div>
                boolean isIWantNotEarlyTimeEnabled = false;
                if (isIWantNotEarlyTimeEnabled) {
                    log.info("time adjustment mechanism");
                    String ourTimeElelemt = page.locator("div.time").last().textContent();
                    boolean timeIsTooEarly = false;
                    int DESIRED_HOUR = 10;
                    if (StringUtils.isNotBlank(ourTimeElelemt)) {
                        String hours = ourTimeElelemt.substring(0, 2);
                        if (Integer.valueOf(hours) > DESIRED_HOUR ) {
                            timeIsTooEarly = true;
                            // add some logic to pick another time. Though this is the last hour among several days.
                        }
                    }
                }

                //confirm reservation modal
                page.getByText("Potwierdź rezerwację").click();
                log.info("Congratulation, the visit was confirmed!");
                isRegistrationDone = true;
                //TODO send message ro something.
            }

            log.info("closing the browser");
            context.close();
        } catch (Exception e) {
            log.warning("Loop failed.");
            log.warning("Exception message: " + e.getMessage());
            log.warning("Exception trace: " + e.getStackTrace());
        }
    }
}