package org.example.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM encryption for individual config values, wrapped as {@code ENC(...)}.
 * The key is derived from a passphrase supplied via the {@code CONFIG_ENCRYPTION_KEY}
 * environment variable, never stored in the repo.
 */
public class ConfigCrypto {

    public static final String ENV_VAR_NAME = "CONFIG_ENCRYPTION_KEY";

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }

    public static SecretKeySpec deriveKey(String passphrase) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(passphrase.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String plaintext, SecretKeySpec key) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

            return ENC_PREFIX + Base64.getEncoder().encodeToString(combined) + ENC_SUFFIX;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt config value", e);
        }
    }

    public static String decrypt(String wrapped, SecretKeySpec key) {
        try {
            String base64 = wrapped.substring(ENC_PREFIX.length(), wrapped.length() - ENC_SUFFIX.length());
            byte[] combined = Base64.getDecoder().decode(base64);
            byte[] iv = Arrays.copyOfRange(combined, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(combined, GCM_IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt config value. Check " + ENV_VAR_NAME + " is correct.", e);
        }
    }
}
