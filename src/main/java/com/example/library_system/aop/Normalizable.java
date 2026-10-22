package com.example.library_system.aop;

public interface Normalizable {

    void normalize();

    static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        return value.strip().replaceAll("\\s+", " ");
    }

    static String normalizeOptionalText(String value) {
        String normalized = normalizeText(value);
        return normalized == null || normalized.isEmpty() ? null : normalized;
    }
}