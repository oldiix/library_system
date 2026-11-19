package com.example.notification.messaging;

import com.example.common.event.LibraryEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MessagingConfig {

    public static final String QUEUE = "notification.library-events";
    public static final String DEAD_LETTER_EXCHANGE = "notification.library-events.dlx";
    public static final String DEAD_LETTER_QUEUE = "notification.library-events.dlq";

    static final String EVENTS_PACKAGE = "com.example.common.event";

    @Bean
    public Declarables notificationTopology() {
        TopicExchange events = ExchangeBuilder.topicExchange(LibraryEvents.EXCHANGE).durable(true).build();

        Queue queue = QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_QUEUE)
                .build();

        DirectExchange deadLetterExchange = new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
        Queue deadLetterQueue = QueueBuilder.durable(DEAD_LETTER_QUEUE).build();

        return new Declarables(
                events,
                queue,
                BindingBuilder.bind(queue).to(events).with(LibraryEvents.BOOK_CREATED),
                BindingBuilder.bind(queue).to(events).with(LibraryEvents.BOOK_DELETED),
                BindingBuilder.bind(queue).to(events).with(LibraryEvents.STOCK_REPORT_GENERATED),
                deadLetterExchange,
                deadLetterQueue,
                BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_QUEUE));
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(EVENTS_PACKAGE);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public ApplicationRunner declareBrokerTopology(AmqpAdmin amqpAdmin) {
        return args -> {
            try {
                amqpAdmin.initialize();
                log.info("Топологію брокера оголошено (черга {})", QUEUE);
            } catch (AmqpException e) {
                log.warn("Брокер повідомлень недоступний, топологію буде оголошено пізніше: {}", e.getMessage());
            }
        };
    }
}