package org.example.utils;

import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.util.Properties;

@Log
public class ConfigReader {

    private static Properties props = new Properties();

    static {
        try {
            props.load(new FileInputStream("src\\main\\resources\\config.properties"));
            decryptEncryptedProperties();
        } catch (Exception e) {
            throw new RuntimeException("Cannot load config.properties, blyat!", e);
        }
    }

    /**
     * Values wrapped as ENC(...) in config.properties are decrypted here using a key
     * derived from the CONFIG_ENCRYPTION_KEY environment variable, so plaintext secrets
     * never need to sit in the file itself.
     */
    private static void decryptEncryptedProperties() {
        boolean hasEncryptedValues = props.stringPropertyNames().stream()
                .anyMatch(name -> ConfigCrypto.isEncrypted(props.getProperty(name)));
        if (!hasEncryptedValues) {
            return;
        }

        String envKey = System.getenv(ConfigCrypto.ENV_VAR_NAME);
        if (StringUtils.isBlank(envKey)) {
            throw new RuntimeException("config.properties contains ENC(...) values but the "
                    + ConfigCrypto.ENV_VAR_NAME + " environment variable is not set.");
        }

        SecretKeySpec key = ConfigCrypto.deriveKey(envKey);
        for (String name : props.stringPropertyNames()) {
            String value = props.getProperty(name);
            if (ConfigCrypto.isEncrypted(value)) {
                props.setProperty(name, ConfigCrypto.decrypt(value, key));
            }
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

    public static Integer getMaxRetryNumber() {
        return Integer.valueOf(props.getProperty("loop.retry.max"));
    }
    public static Integer getRetryIntervalMinutes() {
        return Integer.valueOf(props.getProperty("loop.retry.interval.minutes"));
    }
    public static Integer getNoEmailSleepInterval() {
        return Integer.valueOf(props.getProperty("noemail.sleep.interval"));
    }
    public static String getTelegramBotToken() {
        return props.getProperty("telegram.bot.token");
    }

    public static long getTelegramChatId() {
        return Long.parseLong(props.getProperty("telegram.chat.id"));
    }

    public static boolean getIsBrowserHeadless() {
        boolean isHeadless;
        if (StringUtils.equals(props.getProperty("browser.headless"),"true")) {
            log.info("Browser is headless.");
            isHeadless = true;
        }
        else isHeadless = false;
        return isHeadless;
    }
}
