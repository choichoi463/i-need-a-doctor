package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import lombok.extern.java.Log;
import org.example.browser.BrowserProvider;
import org.example.utils.ConfigReader;
import org.example.utils.MailReader;

@Log
public class Luxmed {
    public static void main(String[] args) {

        try (BrowserProvider browserProvider = new BrowserProvider()) {
            Browser browser = browserProvider.getBrowser();

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            //login part
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
            page.getByText("Kontynuuj bez dodawania").click();
            page.getByText("Dalej").click();

            //wait till mail is received
            mailReader.waitTillNewMailReceived(store, initialMessageCount);
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

            //part with selecting new visit to endokrinolog
            final String VISIT_TYPE_SEARCH_PARAM = "Konsultacja endokrynologiczna";
            final String VISIT_SHORT_NAME = "endo";
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Umów")).click();
            page.locator("input[name^='fake-attribute']").filter().all().get(1).click();
            page.locator("input[name^='fake-attribute']").filter().all().get(1).fill(VISIT_SHORT_NAME);
            page.getByText(VISIT_TYPE_SEARCH_PARAM, new Page.GetByTextOptions().setExact(true)).first().click();

            //part with yes/no options if visible
            page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
            page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
            page.locator("label").filter(new Locator.FilterOptions().setHasText("Tak")).locator("label").click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();
            page.locator("label:nth-child(2) > .checkbox > .form-check-presentation").click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dalej")).click();

            //part with doctor name
            final String DOCTOR_NAME = "Ilona Minkiewicz";
            final String DOCTOR_SHORT_INDICATOR = "ilo";
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wybrano 1 z 5")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Wybrano 1 z 5")).fill(DOCTOR_NAME);
//            page.getByText("Ilona Minkiewicz").first().click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();

            //here TODO add logic to register for any visit, can be doen by other visits ui.

            context.close();
        }
    }
}