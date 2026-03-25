package org.example.utils;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.util.Properties;

@Log
public class ConfigReader {

    private static Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("src\\main\\resources\\config.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Cannot load config.properties, blyat!", e);
        }
    }

    public static String getLuxmedUsername() {
        return props.getProperty("luxmed.username");
    }

    public static String getLuxmedPassword() {
        return props.getProperty("luxmed.password");
    }

    public static boolean getIsBehindTheProxy() {
        boolean isProxy;
        if (StringUtils.equals(props.getProperty("is.proxy"),"true")) {
            log.info("Proxy setup is used.");
            isProxy = true;
        }
        else isProxy = false;
        return isProxy;
    }

    public static String getChromeLocalPath() {
        return props.getProperty("browser.chrome.path");
    }

    public static String getGmailAppPassword() {
        return props.getProperty("gmail.app.password");
    }

    public static String getGmailAppMail() {
        return props.getProperty("gmail.app.mail");
    }
}
