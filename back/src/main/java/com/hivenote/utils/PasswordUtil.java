package com.hivenote.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;

import java.util.regex.Pattern;

public class PasswordUtil {

    private static final Pattern LEGACY_MD5_PATTERN = Pattern.compile("^[a-fA-F0-9]{32}$");
    private static final int BCRYPT_COST = 12;

    private PasswordUtil() {
    }

    public static String encrypt(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    public static boolean verify(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            try {
                return BCrypt.checkpw(inputPassword, storedPassword);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        if (isLegacyMd5Hash(storedPassword)) {
            return SecureUtil.md5(inputPassword).equalsIgnoreCase(storedPassword);
        }

        return false;
    }

    public static boolean needsRehash(String storedPassword) {
        return !isBcryptHash(storedPassword);
    }

    private static boolean isBcryptHash(String value) {
        return value != null
                && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }

    private static boolean isLegacyMd5Hash(String value) {
        return value != null && LEGACY_MD5_PATTERN.matcher(value).matches();
    }
}
