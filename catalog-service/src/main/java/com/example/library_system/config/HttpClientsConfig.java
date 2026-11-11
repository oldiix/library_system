package com.example.library_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class HttpClientsConfig {

    public static final String PARTNER_REST_CLIENT = "partnerRestClient";

    @Bean(name = PARTNER_REST_CLIENT)
    public RestClient partnerRestClient(RestClient.Builder builder,
                                        @Value("${library.external.partner-url}") String partnerUrl,
                                        @Value("${library.external.connect-timeout:1s}") Duration connectTimeout,
                                        @Value("${library.external.read-timeout:3s}") Duration readTimeout) {
        return builder
                .baseUrl(partnerUrl)
                .requestFactory(requestFactory(connectTimeout, readTimeout))
                .build();
    }

    static SimpleClientHttpRequestFactory requestFactory(Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}