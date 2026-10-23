package com.cs360.cs360projecttwo_weighttracker.security;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hashes passwords using PBKDF2 with a random salt. Plain-text passwords are never
 * written for newly created users.
 */
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String STORAGE_PREFIX = "pbkdf2";
    private static final String SEPARATOR = "$";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordHasher() {
        // Utility class.
    }

    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        return STORAGE_PREFIX
                + SEPARATOR
                + Base64.getEncoder().encodeToString(salt)
                + SEPARATOR
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String password, String storedValue) {
        if (password == null) {
            return false;
        }
        if (!isHashed(storedValue)) {
            return password.equals(storedValue);
        }

        String[] parts = storedValue.split("\\$", 3);
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] expected = Base64.getDecoder().decode(parts[2]);
        byte[] actual = pbkdf2(password.toCharArray(), salt);
        return constantTimeEquals(expected, actual);
    }

    public static boolean isHashed(String storedValue) {
        if (storedValue == null) {
            return false;
        }
        String[] parts = storedValue.split("\\$", 3);
        if (parts.length != 3 || !STORAGE_PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            Base64.getDecoder().decode(parts[1]);
            Base64.getDecoder().decode(parts[2]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean constantTimeEquals(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }
        int difference = 0;
        for (int i = 0; i < first.length; i++) {
            difference |= first[i] ^ second[i];
        }
        return difference == 0;
    }
}
