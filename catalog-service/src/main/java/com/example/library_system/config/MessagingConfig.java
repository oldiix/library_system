package com.example.library_system.config;

import com.example.common.event.LibraryEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ExchangeBuilder;
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

    static final String EVENTS_PACKAGE = "com.example.common.event";

    @Bean
    public TopicExchange libraryEventsExchange() {
        return ExchangeBuilder.topicExchange(LibraryEvents.EXCHANGE).durable(true).build();
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
                log.info("Топологію брокера оголошено (exchange {})", LibraryEvents.EXCHANGE);
            } catch (AmqpException e) {
                log.warn("Брокер повідомлень недоступний, топологію буде оголошено пізніше: {}", e.getMessage());
            }
        };
    }
}