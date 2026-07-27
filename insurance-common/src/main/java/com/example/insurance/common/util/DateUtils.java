
package com.example.insurance.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private DateUtils() {}

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter LOCAL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Instant now() {
        return Instant.now();
    }

    public static String formatUtc(Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    public static Instant parseUtc(String dateStr) {
        return dateStr != null ? Instant.parse(dateStr) : null;
    }

    public static String formatLocal(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(LOCAL_FORMATTER) : null;
    }

    public static LocalDateTime parseLocal(String dateStr) {
        return dateStr != null ? LocalDateTime.parse(dateStr, LOCAL_FORMATTER) : null;
    }

    public static LocalDate toLocalDate(Instant instant) {
        return instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant != null ? instant.atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
    }

    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    public static boolean isBefore(Instant first, Instant second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isBefore(second);
    }

    public static boolean isAfter(Instant first, Instant second) {
        if (first == null || second == null) {
            return false;
        }
        return first.isAfter(second);
    }

    public static Instant plusDays(Instant instant, long days) {
        return instant != null ? instant.plusSeconds(days * 24 * 60 * 60) : null;
    }

    public static Instant minusDays(Instant instant, long days) {
        return instant != null ? instant.minusSeconds(days * 24 * 60 * 60) : null;
    }
}