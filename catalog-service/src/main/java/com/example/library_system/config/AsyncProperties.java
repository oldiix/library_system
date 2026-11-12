package com.example.library_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.async")
public record AsyncProperties(int corePoolSize,
                              int maxPoolSize,
                              int queueCapacity,
                              String threadNamePrefix) {
}