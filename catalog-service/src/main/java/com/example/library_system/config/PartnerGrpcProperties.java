package com.example.library_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "library.partner.grpc")
public record PartnerGrpcProperties(String target, Duration deadline) {

    public PartnerGrpcProperties {
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("library.partner.grpc.target must be set");
        }
        if (deadline == null || deadline.isZero() || deadline.isNegative()) {
            throw new IllegalArgumentException("library.partner.grpc.deadline must be a positive duration");
        }
    }
}