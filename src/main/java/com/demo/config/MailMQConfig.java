package com.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailMQConfig {


    public static final String QUEUE = "queue.mail";
    public static final String EXCHANGE = "exchange.direct.mail";
    public static final String ROUTE = "mail";

    public static final String DLX = "exchange.dead-letter.mail";
    public static final String DLQ = "queue.dead-letter.mail";
    public static final String DEAD_LETTER_ROUTE = "dead-letter.mail";

    @Bean
    public DirectExchange mailExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange mailDlx() {
        return ExchangeBuilder.directExchange(DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue mailQueue() {

        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTE)
                .maxLength(1000) //set the max length to prevent too many, and drop the old msg
                .overflow(QueueBuilder.Overflow.dropHead)
                .ttl(60 * 1000)
                .build();

    }

    @Bean
    public Queue mailDlq() {
        return QueueBuilder.durable(DLQ)
                .build();
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(mailQueue())
                .to(mailExchange())
                .with(ROUTE);
    }

    @Bean
    public Binding dlBinding() {
        return BindingBuilder.bind(mailDlq())
                .to(mailDlx())
                .with(DEAD_LETTER_ROUTE);
    }

}
