package com.marketback.main.util;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String cleanPlainText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim()
                .replace("<", "")
                .replace(">", "")
                .replace("\"", "")
                .replace("'", "")
                .replace("`", "");
        if (cleaned.toLowerCase().contains("script")) {
            throw new IllegalArgumentException("input contains unsafe content");
        }
        return cleaned;
    }

    public static void ensureSafeIdentifier(String value) {
        if (value == null || !value.matches("^[A-Za-z0-9_]{3,20}$")) {
            throw new IllegalArgumentException("username format is invalid");
        }
    }

    public static String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cleanPlainText(value);
    }
}
