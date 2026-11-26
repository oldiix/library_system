package com.example.library_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "library.partner.http")
public record PartnerHttpProperties(String baseUrl,
                                    Duration connectTimeout,
                                    Duration readTimeout) {

    public PartnerHttpProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("library.partner.http.base-url must be set");
        }
        requirePositive(connectTimeout, "connect-timeout");
        requirePositive(readTimeout, "read-timeout");
    }

    private static void requirePositive(Duration timeout, String name) {
        if (timeout == null || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("library.partner.http." + name + " must be a positive duration");
        }
    }
}