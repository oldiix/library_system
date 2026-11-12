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
    public static final String NOTIFICATION_REST_CLIENT = "notificationRestClient";

    @Bean(name = PARTNER_REST_CLIENT)
    public RestClient partnerRestClient(RestClient.Builder builder,
                                        @Value("${library.external.partner-url}") String partnerUrl,
                                        @Value("${library.external.connect-timeout:1s}") Duration connectTimeout,
                                        @Value("${library.external.read-timeout:3s}") Duration readTimeout) {
        return builder.clone()
                .baseUrl(partnerUrl)
                .requestFactory(requestFactory(connectTimeout, readTimeout))
                .build();
    }

    @Bean(name = NOTIFICATION_REST_CLIENT)
    public RestClient notificationRestClient(RestClient.Builder builder,
                                             @Value("${library.notifications.url}") String notificationUrl,
                                             @Value("${library.notifications.connect-timeout:1s}") Duration connectTimeout,
                                             @Value("${library.notifications.read-timeout:5s}") Duration readTimeout) {
        return builder.clone()
                .baseUrl(notificationUrl)
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
