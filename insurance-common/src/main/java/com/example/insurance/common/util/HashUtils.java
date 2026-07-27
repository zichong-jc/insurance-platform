
package com.example.insurance.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    private HashUtils() {}

    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String sha256(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String sha256(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hash = digest.digest(input);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public static String sha256(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File not found: " + (file != null ? file.getPath() : "null"));
        }
        try (InputStream is = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}