package org.example.browser;

import com.microsoft.playwright.Page;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BrowserUtils {

    private Page page;

    public BrowserUtils(Page page) {
        this.page = page;
    }

    public void makeScreenshot(Page page, String name) {
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("playwright\\screenshots\\" + name + screenshotNameDateTime())));
    }

    public void makeScreenshot(Page page) {
        makeScreenshot(page, null);
    }

    public void makeScreenshot() {
        makeScreenshot(this.page);
    }

    public void makeScreenshot(String name) {
        makeScreenshot(this.page, name);
    }

    private String screenshotNameDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "screenshot_" + timestamp + ".png";
    }

}
