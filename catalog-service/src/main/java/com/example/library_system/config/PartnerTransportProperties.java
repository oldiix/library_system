package com.example.library_system.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "library.partner")
public record PartnerTransportProperties(PartnerTransport transport) {

    public PartnerTransportProperties {
        if (transport == null) {
            transport = PartnerTransport.HTTP;
        }
    }
}