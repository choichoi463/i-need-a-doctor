package org.example.luxmed;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.SneakyThrows;
import org.example.browser.BrowserUtils;
import org.example.doctor.DoctorType;
import org.example.utils.ConfigReader;
import lombok.extern.java.Log;
import org.example.utils.MailReader;
import org.example.utils.Telegram;

import java.time.Duration;
import java.util.List;

import static com.microsoft.playwright.options.LoadState.NETWORKIDLE;
import static org.example.utils.ConfigReader.getNoEmailSleepInterval;

@Log
public class LuxmedPage {

    private Page page;
    private BrowserUtils browserUtils;

    public LuxmedPage(Page page) {
        this.page = page;
        this.browserUtils = new BrowserUtils(page);
    }

    public void login() {
        try {
            //login part
            log.info("login part");
            page.navigate("https://portalpacjenta.luxmed.pl/PatientPortal/NewPortal/Page/Account/Login?returnUrl=%2FPage%2FDashboard");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).fill(ConfigReader.getLuxmedUsername());
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz login")).press("Tab");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wpisz hasło")).fill(ConfigReader.getLuxmedPassword());
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Zaloguj się")).click();

        } catch (Exception e) {
            log.severe("Login failed.");
            browserUtils.makeScreenshot("login_failed_");
            throw new RuntimeException(e);
        }
    }

    public void emailVerification() {
        try {
            emailVerifiationLogic();
        } catch (Exception e) {
            log.severe("Email verification part failed.");
            browserUtils.makeScreenshot("email_verification_failed_");
            throw new RuntimeException(e);
        }
    }

    private void emailVerifiationLogic() {
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
    }

    @SneakyThrows
    public void optionalAnketaQuestionPomin() {
        page.waitForLoadState();
        Thread.sleep(5000);

        try {
            Locator pominStomatologiaButton = page.getByText("Pomiń");
            if (pominStomatologiaButton.isVisible()) {
                log.info("Pomin stomatologia button is visible");
                pominStomatologiaButton.click();
            }
        }
        catch (Exception e) {
            log.severe("Optional Stomatologia question part failed.");
            browserUtils.makeScreenshot("optional_pomin_stomatologia_failed_");
            throw new RuntimeException(e);
        }

            //TODO if present
        try {
            Locator anketa = page.locator("iframe[title=\"Zaproszenie do ankiety\"]").contentFrame().getByRole(AriaRole.BUTTON, new FrameLocator.GetByRoleOptions().setName("Przypomnij mi później"));
            if (anketa.isVisible()) {
                log.info("closing anketa");
                anketa.click();
            }
        } catch (Exception e) {
            log.severe("Processing Anketa optional questions has failed");
            browserUtils.makeScreenshot("anketa_questions_failed_");
            throw new RuntimeException(e);
        }
    }

    private void selectingNewVisitLogic(DoctorType doctorType) {
        log.info("choosing a visit type: " + doctorType.getDisplayName());
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Umów")).click();
        page.locator("input[name^='fake-attribute']").filter().all().get(1).click();
        page.locator("input[name^='fake-attribute']").filter().all().get(1).fill(doctorType.getVisitShortCode());
        page.getByText(doctorType.getVisitTypeSearchParam(), new Page.GetByTextOptions().setExact(true)).first().click();
    }

    public void selectingNewVisit(DoctorType doctorType) {
        try {
            selectingNewVisitLogic(doctorType);
        } catch (Exception e) {
            log.severe("Selecting a visit type has failed");
            browserUtils.makeScreenshot("visit_type_selection_failed_");
            throw new RuntimeException(e);
        }
    }

    public void isThatYourFirstVisitQuestions() {
        // part "is it your first visit?"
        //todo here add a proper text locator, cause those question might be different.
        //todo understand if it is needed or not
//        if (page.getByText("Czy jest to Twoja pierwszorazowa wizyta u endokrynologa w LUX MED?").isVisible()) {
//            page.locator("label[for='answerId_1']").last().click();
//            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
//        }
    }

    private void partWithYesNoQuestionsLogicEndokrynolog() throws InterruptedException {
        //part with yes/no options if visible
        log.info("Endo questions starter");
        Thread.sleep(Duration.ofSeconds(5));
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
    }

    private void partWithYesNoQuestionsGastrologTelLogic() throws InterruptedException {
        log.info("Gastro questions starter - kontrolna");
        Thread.sleep(Duration.ofSeconds(5));
        //TODO add questions answers here after
    }

    private void partWithYesNoQuestionsGastrologLogic() throws InterruptedException {
        log.info("Gastro questions  w placowkie questionaire.");
        Thread.sleep(Duration.ofSeconds(5));


    }

    private void followupRegistrationFormAccept() throws InterruptedException {
        log.info("Followup visit logic.");
        Thread.sleep(Duration.ofSeconds(5));
        // form appears only when a skirowanie (referral) exists, otherwise there is no popup
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Wybierz wizytę kontrolną")).click();
    }

    public void partWithYesNoQuestionsAboutVisitType(DoctorType doctorType, boolean isFollowUp) {
        try {
            if (isFollowUp) {
                followupRegistrationFormAccept();
            }
            else {
                switch (doctorType) {
                    case ENDOKRYNOLOG :
                        partWithYesNoQuestionsLogicEndokrynolog();
                        break;
                    case OKULISTA:
                        //TODO implementation for various optional questions for first visit
                        break;
                    case GASTROSKOPIJA_TELEFONICZNA:
                        partWithYesNoQuestionsGastrologTelLogic();
                        break;
                    case GASTROSKOPIJA:
                        partWithYesNoQuestionsGastrologLogic();
                        break;
                }
            }

        } catch (Exception e) {
            log.severe("Part with yes/no questions has failed");
            browserUtils.makeScreenshot("yes_no_questions_failed_");
            throw new RuntimeException(e);
        }
    }

    private void chooseDoctorNameAndClinicLogic(String doctorName, boolean isFollowupVisit) {
        //part with doctor name
        log.info("filtering by a doctor name: " + doctorName);

        if (isFollowupVisit) {
            log.info("looking for a followup visit, any results first.");
            //TODO add logic for searching for any doctor name, and filter those later on search results page.
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();
        }

        if (!isFollowupVisit) {
            //The first time ever this page is reached the doctor name picker will have "Dowolny lekarz" each time.
            //If ever the name will be picked - the locator will change into "Wybrano 1 z 5", that is why if else loop is introduced here
//        page.waitForTimeout(2500);
            page.waitForLoadState(NETWORKIDLE);
            if (page.getByText("Wyczyść").isVisible() == true) {
                log.info("Some doctors are already filled in, going to clear those.");
                page.getByText("Wyczyść").click();
            }

            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).fill(doctorName);
            page.getByRole(AriaRole.LISTITEM).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click(); //clicks to make a dropdown disappear fisrt
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();
            //TODO clinic is now - default one
        }
    }

    public void chooseDoctorNameAndClinic(String doctorName, boolean isFollowupVisit) {
        try {
            chooseDoctorNameAndClinicLogic(doctorName, isFollowupVisit);
        } catch (Exception e) {
            log.severe("Choose doctor anme has failed");
            browserUtils.makeScreenshot("choose_doctor_name_failed_");
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param isFollowupVisit - used when need to apply addition filtering over results page for a followup visit
     * @param doctorName - filter doctor name for a followup visit
     * @return boolean of registration done or not to breka the loop
     * @throws InterruptedException
     */
    private boolean selectAVisitFromTheListLogic(boolean isFollowupVisit, String doctorName) throws InterruptedException {
        //list of all found terms
        //if terms have something in the list
        Thread.sleep(Duration.ofSeconds(15)); //TODO may be replace later by some dynamic element, but results are loaded slow, with no elements at all
        boolean isRegistrationDone = false;
        log.info("looking through found available results");
        browserUtils.makeScreenshot("LOOKING_RESULTS_");

        if (isFollowupVisit) {
            //results page, with filtering by specific doctor
            log.info("Going to apply doctor filter on resutls page for a followup visit");
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).fill(doctorName);
            page.locator("label").nth(1).click();
            // below is ame as above click.
            //page.locator("//*[contains(text(),\"" + doctorName + "\")]").click();
            //click outside the form
            page.locator("css=.dropdown-icon-opened").click();

            Thread.sleep(Duration.ofSeconds(15));
            log.info("Filtered the results by specific doctor.");
            browserUtils.makeScreenshot("LOOKING_RESULTS_FILTERED_");
        }

        if (page.locator("css=app-term").count() > 0) {
            log.info("selecting the available timeslot reservation slot. Time 12+");

            //applying time filter on ui
//              page.getByText("Dowolna").click();
//                page.getByText("Do 12:00").click();
            page.getByText("12:00 - 17:00").first().click();
            page.getByText("Po 17:00").first().click();

            //expand all visits to be viewed
            Locator expandVisits = page.locator("a.float-right").first();
            if (expandVisits.isVisible()) {
                log.info("Expanding to view all visits in the days");
                for (Locator expand : page.locator("a.float-right").all()) {
                    expand.click();
                }
                log.info("Visits expanded.");
            }

            //per days
            /**
             * $("div.card-header-content").textContent
             * '14 kwi, wtorek14 kwietnia, wtorek (4)Dostępne terminy: 4 od 09:00 do 12:00'
             */

            // Collecting all available elements, collecting indexes
            //groups of time blocks per day.
            //page.locator("ul.list-group.ng-star-inserted")
            List<Locator> daysList = page.locator("ul.list-group.ng-star-inserted").all();
            for (int i = 0; i < daysList.size(); i++) {
                //looking for time in the day
                List<Locator> timePerDayList = daysList.get(i).locator("div.time").all();
                for (int j = timePerDayList.size() - 1; j >= 0; j--) {
                    String textTime = timePerDayList.get(j).textContent();
                    //example of text: '09:00'

                    //start clicking to confirm a visit
                    log.info("Confirming the visit at " + textTime);
                    timePerDayList.get(j).hover();
                    page.locator("css=div.reserve-button>button").last().click();
                }
            }

            //confirm reservation modal
            page.getByText("Potwierdź rezerwację").click();
            log.info("Congratulation, the visit was confirmed!");
            isRegistrationDone = true;
            //TODO send message ro something.
            browserUtils.makeScreenshot("RESERVATION_TIME_SUCCESS_");
            Telegram telegram = new Telegram();
            telegram.sendMessage("Reservation was done successfully, please check your mailbox for a confirmation.");
        }
        return isRegistrationDone;
    }

    public boolean selectAVisitFromTheList(boolean isFollowupVisit, String  doctorName) throws InterruptedException {
        try {
            return selectAVisitFromTheListLogic(isFollowupVisit, doctorName);
        } catch (Exception e) {
            log.severe("Selecting a visit from found results has failed");
            browserUtils.makeScreenshot("selecting_visit_failed_");
            throw new RuntimeException(e);
        }
    }
}
