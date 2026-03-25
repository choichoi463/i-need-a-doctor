package org.example.utils;

import java.io.FileInputStream;
import java.util.Properties;

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
        return Boolean.getBoolean(props.getProperty("is.proxy"));
    }

    public static String getChromeLocalPath() {
        return props.getProperty("browser.chrome.path");
    }
}
