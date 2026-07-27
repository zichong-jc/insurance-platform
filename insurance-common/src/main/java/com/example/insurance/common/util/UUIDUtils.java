
package com.example.insurance.common.util;

import java.util.UUID;

public final class UUIDUtils {

    private UUIDUtils() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static String generateWithoutHyphen() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static UUID fromString(String uuidStr) {
        return UUID.fromString(uuidStr);
    }

    public static boolean isValid(String uuidStr) {
        if (uuidStr == null) {
            return false;
        }
        try {
            UUID.fromString(uuidStr);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}