package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import lombok.extern.java.Log;
import org.example.browser.BrowserProvider;
import org.example.utils.ConfigReader;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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

            //part with selecting new visit to endokrinolog
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Umów")).click();
            page.locator("input[name=\"fake-attribute-2026-03-04T17:34:29.514Z-0.9195727907460862\"]").click();
            page.locator("input[name=\"fake-attribute-2026-03-04T17:34:29.514Z-0.9195727907460862\"]").fill("endo");
            page.getByText("Konsultacja endokrynologiczna", new Page.GetByTextOptions().setExact(true)).click();

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
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).click();
            page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Dowolny lekarz")).fill("ilo");
            page.getByText("Ilona Minkiewicz").click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Szukaj")).click();

            //here TODO add logic to register for any visit, can be doen by other visits ui.

            context.close();
        }
    }
}