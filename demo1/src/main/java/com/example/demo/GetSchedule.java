package com.example.demo;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetSchedule {
    @Autowired
    AmqpTemplate amqpTemplate;
    @Autowired
    public void receiveTrains() {
        amqpTemplate.convertAndSend("schedule", "schedule.sendSchedule", "sendSchedule");
    }
}
