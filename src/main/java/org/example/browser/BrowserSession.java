package org.example.browser;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;

/**
 * This class serves the purpose to keep the same browser and page all the time, keeping cookies alive.
 */
public final class BrowserSession {
    private static BrowserProvider provider;
    private static BrowserContext context;
    private static Page page;

    private BrowserSession() {}

    public static synchronized Page getPage() {
        if (provider == null) {
            provider = new BrowserProvider();
            Runtime.getRuntime().addShutdownHook(new Thread(BrowserSession::close));
        }
        if (page == null || page.isClosed()) {
            if (context == null) {
                context = provider.getBrowser().newContext();
            }
            page = context.newPage();
//            page.navigate("https://www.google.com");
        }
        return page;
    }

    public static synchronized void close() {
        try {
            if (provider != null) provider.close();
        } finally {
            provider = null;
            context = null;
            page = null;
        }
    }
}