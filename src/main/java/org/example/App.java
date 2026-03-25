package org.example;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class App {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setExecutablePath(Paths.get("src\\main\\resources\\browser\\chrome-win2\\chrome.exe")); // <-- your custom path here
            options.setHeadless(false);
            options.setSlowMo(50);
            options.setDownloadsPath(Paths.get("playwright\\downloads"));


            Browser browser = playwright.chromium().launch(options);

            //optional - context of a browser for each test
//            BrowserContext context = browser.newContext();
//            Page page = context.newPage();

            Page page = browser.newPage();
            page.navigate("https://playwright.dev");
            System.out.println(page.title());

            String SCREENSHOT_PATH = "playwright\\screenshots";
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(SCREENSHOT_PATH+ "example.png")));




            // Expect a title "to contain" a substring.
            assertThat(page).hasTitle(Pattern.compile("Playwright"));

            // create a locator
            Locator getStarted = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Get Started"));

            // Expect an attribute "to be strictly equal" to the value.
            assertThat(getStarted).hasAttribute("href", "/docs/intro");

            // Click the get started link.
            getStarted.click();

            // Expects page to have a heading with the name of Installation.
            assertThat(page.getByRole(AriaRole.HEADING,
                    new Page.GetByRoleOptions().setName("Installation"))).isVisible();

            browser.close();
        }
    }
}
