package com.example.producer;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProducerApplication {

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange platformExchange() {
        return ExchangeBuilder.topicExchange("arrival").build();
    }
    @Bean
    TopicExchange departureExchange() {
        return ExchangeBuilder.topicExchange("departure").build();
    }

    @Bean
    TopicExchange scheduleExchange() {
        return ExchangeBuilder.topicExchange("schedule").build();
    }

    @Bean
    Queue trainsQueue() {return QueueBuilder.durable("sendSchedule").build();}

    @Bean
    Binding scheduleToTrainsBinding() {return BindingBuilder.bind(trainsQueue()).to(scheduleExchange()).with("schedule.sendSchedule");}

    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }


}
