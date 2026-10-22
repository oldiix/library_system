package com.example.library_system.aop;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Вимикачі функцій із application.yml (секція library.features).
 * Функція, якої немає в налаштуваннях, вважається вимкненою.
 */
@ConfigurationProperties(prefix = "library")
public record FeatureProperties(Map<String, Boolean> features) {

    public FeatureProperties {
        features = features == null ? Map.of() : Map.copyOf(features);
    }

    public boolean isEnabled(String feature) {
        return features.getOrDefault(feature, false);
    }
}