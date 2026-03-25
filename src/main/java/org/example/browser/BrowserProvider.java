package org.example.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import lombok.extern.java.Log;
import org.example.utils.ConfigReader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log
public class BrowserProvider implements AutoCloseable {

    private Playwright playwright;
    private Browser browser;

    public Browser getBrowser() {
        if (browser != null) {
            return browser;
        }

        try {
            playwright = Playwright.create();

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()

                    .setHeadless(false)
                    .setSlowMo(50)
                    .setDownloadsPath(Paths.get("playwright\\downloads"));

            if (ConfigReader.getIsBehindTheProxy()) {
                System.setProperty("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
                options.setExecutablePath(Paths.get(ConfigReader.getChromeLocalPath()));
            }

            Path downloadsDir = Paths.get("playwright", "downloads");
            Files.createDirectories(downloadsDir);

            browser = playwright.chromium().launch(options);
            return browser;
        } catch (Exception e) {
            log.warning("Browser failed to initialize!");
            close();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (browser != null) {
                browser.close();
            }
        } catch (Exception ignored) {
            // ignore
        } finally {
            browser = null;
        }

        try {
            if (playwright != null) {
                playwright.close();
            }
        } catch (Exception ignored) {
            // ignore
        } finally {
            playwright = null;
        }
    }
}