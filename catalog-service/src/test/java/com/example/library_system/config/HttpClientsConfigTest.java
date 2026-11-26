package com.example.library_system.config;

import com.example.library_system.partner.PartnerHttpApi;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HttpClientsConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(RestClient.Builder.class, RestClient::builder)
            .withUserConfiguration(PropertiesConfig.class, HttpClientsConfig.class);

    @Test
    void timeoutsAndUrlComeFromProperties() {
        contextRunner
                .withPropertyValues(
                        "library.partner.http.base-url=http://partner.test:8082",
                        "library.partner.http.connect-timeout=500ms",
                        "library.partner.http.read-timeout=1500ms")
                .run(context -> {
                    assertThat(context).hasSingleBean(PartnerHttpApi.class);
                    PartnerHttpProperties properties = context.getBean(PartnerHttpProperties.class);
                    assertThat(properties.baseUrl()).isEqualTo("http://partner.test:8082");
                    assertThat(properties.connectTimeout()).isEqualTo(Duration.ofMillis(500));
                    assertThat(properties.readTimeout()).isEqualTo(Duration.ofMillis(1500));
                });
    }

    @Test
    void missingBaseUrl_failsAtStartup() {
        contextRunner
                .withPropertyValues(
                        "library.partner.http.connect-timeout=1s",
                        "library.partner.http.read-timeout=2s")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void zeroReadTimeout_failsAtStartup() {
        contextRunner
                .withPropertyValues(
                        "library.partner.http.base-url=http://partner.test:8082",
                        "library.partner.http.connect-timeout=1s",
                        "library.partner.http.read-timeout=0s")
                .run(context -> assertThat(context).hasFailed());
    }

    @EnableConfigurationProperties(PartnerHttpProperties.class)
    static class PropertiesConfig {
    }
}