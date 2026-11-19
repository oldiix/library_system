package com.example.notification.messaging;

import com.example.common.event.LibraryEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessagingConfigTest {

    private final MessagingConfig config = new MessagingConfig();

    @Test
    void notificationTopology_durableQueueWithDeadLetteringBoundToAllThreeEvents() {
        Declarables topology = config.notificationTopology();

        Queue queue = topology.getDeclarablesByType(Queue.class).stream()
                .filter(q -> q.getName().equals(MessagingConfig.QUEUE))
                .findFirst().orElseThrow();
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.getArguments())
                .containsEntry("x-dead-letter-exchange", MessagingConfig.DEAD_LETTER_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", MessagingConfig.DEAD_LETTER_QUEUE);

        assertThat(topology.getDeclarablesByType(Binding.class))
                .filteredOn(binding -> binding.getExchange().equals(LibraryEvents.EXCHANGE))
                .extracting(Binding::getRoutingKey)
                .containsExactlyInAnyOrder(
                        LibraryEvents.BOOK_CREATED,
                        LibraryEvents.BOOK_DELETED,
                        LibraryEvents.STOCK_REPORT_GENERATED);
    }

    @Test
    void messageConverter_typeOutsideEventsPackage_isRejected() {

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
        MessageConverter converter = config.messageConverter(objectMapper);
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setHeader("__TypeId__", "java.io.File");
        Message suspicious = new Message("\"C:/secret.txt\"".getBytes(StandardCharsets.UTF_8), properties);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> converter.fromMessage(suspicious));

        assertThat(exception.getMessage()).contains("java.io.File", "not in the trusted packages");
    }
}