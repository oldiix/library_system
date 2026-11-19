package com.example.library_system.config;

import com.example.common.event.BookCreatedEvent;
import com.example.common.event.LibraryEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingConfigTest {

    private final MessagingConfig config = new MessagingConfig();

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Test
    void libraryEventsExchange_isDurableTopicExchange() {
        TopicExchange exchange = config.libraryEventsExchange();

        assertThat(exchange.getName()).isEqualTo(LibraryEvents.EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
    }

    @Test
    void messageConverter_event_isSentAsPersistentJsonWithTypeHeaderAndReadBack() {
        MessageConverter converter = config.messageConverter(objectMapper);
        BookCreatedEvent event = new BookCreatedEvent(UUID.randomUUID(),
                Instant.parse("2026-11-19T20:00:00Z"), 7L, "Kobzar", "Taras Shevchenko");

        Message message = converter.toMessage(event, new MessageProperties());
        Object readBack = converter.fromMessage(message);

        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        assertThat(json).contains("\"title\":\"Kobzar\"", "\"occurredAt\":\"2026-11-19T20:00:00Z\"");

        assertThat(message.getMessageProperties().getHeaders().get("__TypeId__"))
                .isEqualTo(BookCreatedEvent.class.getName());
        assertThat(message.getMessageProperties().getDeliveryMode()).isEqualTo(MessageDeliveryMode.PERSISTENT);

        assertThat(readBack).isEqualTo(event);
    }
}