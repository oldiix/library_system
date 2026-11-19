package com.example.notification.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class BrokerSettingsTest {

    @Test
    void listener_acknowledgesAfterSuccess_retriesLimitedTimes_thenSendsToDeadLetterQueue() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();

        assertThat(environment.getProperty("spring.rabbitmq.listener.simple.acknowledge-mode")).isEqualTo("auto");
        assertThat(environment.getProperty("spring.rabbitmq.listener.simple.retry.enabled")).isEqualTo("true");
        assertThat(environment.getProperty("spring.rabbitmq.listener.simple.retry.max-attempts")).isEqualTo("3");
        assertThat(environment.getProperty("spring.rabbitmq.listener.simple.default-requeue-rejected")).isEqualTo("false");
    }

    @Test
    void brokerHost_withoutEnvironmentVariable_isLocalhost() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();
        environment.getPropertySources().remove(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME);

        assertThat(environment.getProperty("spring.rabbitmq.host")).isEqualTo("localhost");
        assertThat(environment.getProperty("spring.rabbitmq.port")).isEqualTo("5672");
    }

    @Test
    void brokerHost_withEnvironmentVariable_isTakenFromIt() throws IOException {
        StandardEnvironment environment = environmentFromApplicationYml();
        environment.getPropertySources().addFirst(new MapPropertySource("docker-env", Map.of("RABBITMQ_HOST", "rabbitmq")));

        assertThat(environment.getProperty("spring.rabbitmq.host")).isEqualTo("rabbitmq");
    }

    @Test
    void composeFile_passesBrokerServiceNameToNotificationService() {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new FileSystemResource(composeFile()));

        Properties compose = yaml.getObject();

        assertThat(compose).isNotNull();
        assertThat(compose.getProperty("services.notification-service.environment.RABBITMQ_HOST")).isEqualTo("rabbitmq");
        assertThat(compose.getProperty("services.rabbitmq.image")).startsWith("rabbitmq:");
    }

    private static StandardEnvironment environmentFromApplicationYml() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yml"));
        sources.forEach(source -> environment.getPropertySources().addLast(source));
        return environment;
    }

    private static Path composeFile() {
        Path fromModule = Path.of("..", "compose.yaml");
        return Files.exists(fromModule) ? fromModule : Path.of("compose.yaml");
    }
}
