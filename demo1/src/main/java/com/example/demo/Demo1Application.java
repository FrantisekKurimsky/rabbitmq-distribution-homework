package com.example.demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RestController
@SpringBootApplication
@EnableScheduling
@EnableRabbit //need for activation of handler for annotation @RabbitListener
public class Demo1Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo1Application.class, args);
    }

    public static final Logger logger = LoggerFactory.getLogger(Demo1Application.class);
    private ConcurrentMap<String,Train> trains = new ConcurrentHashMap<>();
    boolean canGo = false;
    Train [] platformTrains = new Train[5];

    @Scheduled(fixedDelay = 2000, initialDelay = 10000)
    public void check() {
        Date date = new Date();
        for (Map.Entry<String, Train> entry : trains.entrySet()) {
            if (entry.getValue().getArrival().compareTo(date)<=0 && entry.getValue().getPlatform()==0){
                entry.getValue().setDelay((date.getTime()-entry.getValue().getArrival().getTime())/1000);
            }
        }

    }
    @RequestMapping("/all")
    @CrossOrigin
    public List<Train> getAll() {
        List<Train> valuesList = new ArrayList<>(trains.values());
        valuesList.sort(Comparator.comparing(Train::getDeparture));
        List<Train> finalList = new ArrayList<>();
        int number = 20;
        if (valuesList.size()<20){
            number = valuesList.size();
        }
        for (int i = 0; i < number; i++) {
            finalList.add(valuesList.get(i));
        }
        return finalList;
    }

    @RequestMapping("/platforms")
    @CrossOrigin
    public List<Train> platforms() {
        List<Train> finalne = new ArrayList<>();
        for (int i = 0; i < platformTrains.length; i++) {
            Train nowTrain = platformTrains[i];
            if (nowTrain!=null){
                finalne.add(nowTrain);
            }
        }
        return finalne;
    }

    @Bean
    TopicExchange arrivalExchange() {return ExchangeBuilder.topicExchange("arrival").build();}
    @Bean
    TopicExchange departureExchange() {return ExchangeBuilder.topicExchange("departure").build();}
    @Bean
    TopicExchange scheduleExchange() {return ExchangeBuilder.topicExchange("schedule").build();}

    @Bean
    Queue platform1Queue() {return QueueBuilder.durable("platform1").build();}
    @Bean
    Queue platform2Queue() {return QueueBuilder.durable("platform2").build();}
    @Bean
    Queue platform3Queue() {return QueueBuilder.durable("platform3").build();}
    @Bean
    Queue platform4Queue() {return QueueBuilder.durable("platform4").build();}
    @Bean
    Queue platform5Queue() {return QueueBuilder.durable("platform5").build();}

    @Bean
    Queue d1Queue() {return QueueBuilder.durable("1").build();}
    @Bean
    Queue d2Queue() {return QueueBuilder.durable("2").build();}
    @Bean
    Queue d3Queue() {return QueueBuilder.durable("3").build();}
    @Bean
    Queue d4Queue() {return QueueBuilder.durable("4").build();}
    @Bean
    Queue d5Queue() {return QueueBuilder.durable("5").build();}

    @Bean
    Queue trainsQueue() {return QueueBuilder.durable("trains").build();}

    @Bean
    Binding scheduleToTrainsBinding() {return BindingBuilder.bind(trainsQueue()).to(scheduleExchange()).with("schedule.trains");}

    @Bean
    Binding departureToD1Binding() {return BindingBuilder.bind(platform1Queue()).to(arrivalExchange()).with("arrival.platform1"); }
    @Bean
    Binding departureToD2Binding() {return BindingBuilder.bind(platform2Queue()).to(arrivalExchange()).with("arrival.platform2");}
    @Bean
    Binding departureToD3Binding() {return BindingBuilder.bind(platform3Queue()).to(arrivalExchange()).with("arrival.platform3");}
    @Bean
    Binding departureToD4Binding() {return BindingBuilder.bind(platform4Queue()).to(arrivalExchange()).with("arrival.platform4");}
    @Bean
    Binding departureToD5Binding() {return BindingBuilder.bind(platform5Queue()).to(arrivalExchange()).with("arrival.platform5");}

    @Bean
    Binding platformToPlatform1Binding() {return BindingBuilder.bind(d1Queue()).to(departureExchange()).with("departure.1"); }
    @Bean
    Binding platformToPlatform2Binding() {return BindingBuilder.bind(d2Queue()).to(departureExchange()).with("departure.2");}
    @Bean
    Binding platformToPlatform3Binding() {return BindingBuilder.bind(d3Queue()).to(departureExchange()).with("departure.3");}
    @Bean
    Binding platformToPlatform4Binding() {return BindingBuilder.bind(d4Queue()).to(departureExchange()).with("departure.4");}
    @Bean
    Binding platformToPlatform5Binding() {return BindingBuilder.bind(d5Queue()).to(departureExchange()).with("departure.5");}



    @RabbitListener(queues = "trains")
    public void receiveTrains(Map<String,Train> recievedTrains) {
        trains = new ConcurrentHashMap<>(recievedTrains);
        canGo = true;
        for (Map.Entry<String, Train> entry : trains.entrySet()) {
            logger.info("checking train from schedule {}, with arrival : {}, and departure {}",entry.getKey(),entry.getValue().getArrival(), entry.getValue().getDeparture());
        }
    }

    @RabbitListener(queues = "platform1")
    public void receiveTrain1(Train train) {
        if (!canGo) {
            throw new IllegalArgumentException("message");
        }else{
            train.setPlatform(1);
            platformTrains[0]=train;
            arrivedTrain(train, 1);

        }

    }
    @RabbitListener(queues = "platform2")
    public void receiveTrain2(Train train) {
        if (!canGo) {
            throw new IllegalArgumentException("message");
        }else{
            train.setPlatform(2);
            platformTrains[1]=train;
            arrivedTrain(train, 2);

        }

    }
    @RabbitListener(queues = "platform3")
    public void receiveTrain3(Train train) {
        if (!canGo) {
            throw new IllegalArgumentException("message");
        }else{
            train.setPlatform(3);
            platformTrains[2]=train;
            arrivedTrain(train, 3);

        }

    }
    @RabbitListener(queues = "platform4")
    public void receiveTrain4(Train train) {
        if (!canGo) {
            throw new IllegalArgumentException("message");
        }else{
            train.setPlatform(4);
            platformTrains[3]=train;
            arrivedTrain(train, 4);

        }

    }
    @RabbitListener(queues = "platform5")
    public void receiveTrain5(Train train) {
        if (!canGo) {
            throw new IllegalArgumentException("message");
        }else{
            train.setPlatform(5);
            platformTrains[4]=train;
            arrivedTrain(train, 5);

        }

    }
    @RabbitListener(queues = "1")
    public void departureTrain1(Train train) {
        if (!canGo){
            throw new IllegalArgumentException("message");
        }else{
            exitedTrain(train, 1);
            platformTrains[0]=null;
        }

    }
    @RabbitListener(queues = "2")
    public void departureTrain2(Train train) {
        if (!canGo){
            throw new IllegalArgumentException("message");
        }else{
            exitedTrain(train, 2);
            platformTrains[1]=null;
        }

    }
    @RabbitListener(queues = "3")
    public void departureTrain3(Train train) {
        if (!canGo){
            throw new IllegalArgumentException("message");
        }else{
            exitedTrain(train, 3);
            platformTrains[2]=null;
        }

    }
    @RabbitListener(queues = "4")
    public void departureTrain4(Train train) {
        if (!canGo){
            throw new IllegalArgumentException("message");
        }else{
            exitedTrain(train, 4);
            platformTrains[3]=null;
        }

    }
    @RabbitListener(queues = "5")
    public void departureTrain5(Train train) {
        if (!canGo){
            throw new IllegalArgumentException("message");
        }else{
            exitedTrain(train, 5);
            platformTrains[4]=null;
        }

    }

    private void exitedTrain(Train train, int platform){
        Train trainFromMap = trains.get(train.getType()+train.getNumber());
        if (trainFromMap == null){
            System.out.println("Train is not relevant");
            return;
        }
        if (trainFromMap.getPlatform()==0){
            throw new IllegalArgumentException("este vlak neprisiel");
        }
        trainFromMap.setDelay(train.getDelay());
        trains.remove(trainFromMap.getType()+trainFromMap.getNumber());
        logger.info("exited train {} on platform {}, with arrival : {}, and departure {} and delay: {}",trainFromMap.getType()+trainFromMap.getNumber(),platform,trainFromMap.getArrival().getMinutes(), trainFromMap.getDeparture().getMinutes(), trainFromMap.getDelay());
    }
    private void arrivedTrain(Train train, int platform){
        Train trainFromMap = trains.get(train.getType()+train.getNumber());
        if (trainFromMap == null){
            System.out.println("Train is not relevant");
            return;
        }
        trainFromMap.setDelay(train.getDelay());
        trainFromMap.setPlatform(platform);
        logger.info("arrived train {} on platform {}, with arrival : {}, and departure {} and delay: {}",trainFromMap.getType()+trainFromMap.getNumber(),platform,trainFromMap.getArrival().getMinutes(), trainFromMap.getDeparture().getMinutes(), trainFromMap.getDelay());
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


}
