package com.example.library_system.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BrokerAddressTest {

    @Test
    void brokerHost_withoutEnvironmentVariable_isLocalhost() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);

        assertThat(environment.getProperty("spring.rabbitmq.host")).isEqualTo("localhost");
        assertThat(environment.getProperty("spring.rabbitmq.username")).isEqualTo("library");
    }

    @Test
    void brokerHost_withEnvironmentVariable_isTakenFromIt() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();
        environment.getPropertySources().addFirst(new MapPropertySource("docker-env",
                Map.of("RABBITMQ_HOST", "rabbitmq", "RABBITMQ_PORT", "5673")));

        assertThat(environment.getProperty("spring.rabbitmq.host")).isEqualTo("rabbitmq");
        assertThat(environment.getProperty("spring.rabbitmq.port")).isEqualTo("5673");
    }

    @Test
    void catalogConfiguration_hasNoNotificationServiceAddress() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();

        assertThat(environment.getProperty("library.notifications.url")).isNull();
    }

    private static StandardEnvironment environmentFromApplicationYml() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"))
                .forEach(source -> environment.getPropertySources().addLast(source));
        return environment;
    }
}
