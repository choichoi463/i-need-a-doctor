package org.example;

import org.apache.commons.lang3.StringUtils;
import org.example.utils.ConfigCrypto;

import javax.crypto.spec.SecretKeySpec;

/**
 * CLI helper to encrypt a config value for pasting into config.properties as ENC(...).
 * Set the CONFIG_ENCRYPTION_KEY environment variable first, then run with the plaintext
 * value as the argument, e.g.:
 * mvn exec:java -D exec.mainClass=org.example.ConfigEncryptorMain -D exec.args="my-secret-password"
 * Multi-word values get split into separate args by the shell/exec-plugin's own quoting rules
 * (exec.args needs to be quoted twice - once for the shell, once for the plugin), so all args
 * passed here are rejoined with a single space rather than requiring exact quoting.
 */
public class ConfigEncryptorMain {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: pass the plaintext value to encrypt as the argument(s).");
            return;
        }

        String envKey = System.getenv(ConfigCrypto.ENV_VAR_NAME);
        if (StringUtils.isBlank(envKey)) {
            throw new RuntimeException(ConfigCrypto.ENV_VAR_NAME + " environment variable is not set.");
        }

        String plaintext = String.join(" ", args);
        SecretKeySpec key = ConfigCrypto.deriveKey(envKey);
        System.out.println(ConfigCrypto.encrypt(plaintext, key));
    }
}
