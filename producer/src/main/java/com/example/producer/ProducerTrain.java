package com.example.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ProducerTrain {

    public static final Logger logger = LoggerFactory.getLogger(ProducerApplication.class);


    Map<String, Train> generatedTrains = Train.generateTrains();
    Train[] platformTrains = new Train[5];
    int counter = 0;

    @Autowired
    AmqpTemplate amqpTemplate;

    //  sending schedule of trains in our station

    @RabbitListener(queues = "sendSchedule")
    public void receiveTrains(String message) {
        if (message.equals("sendSchedule"))
            amqpTemplate.convertAndSend("schedule", "schedule.trains", generatedTrains);
    }

    @Scheduled(fixedDelay = 2000, initialDelay = 10000)
    public void platform1ServiceArrival() {

        if (platformTrains[counter] == null && generatedTrains.size() > 0) {

            List<Train> valuesList = new ArrayList<>(generatedTrains.values());
            platformTrains[counter]  = valuesList.get((int) (Math.random() * valuesList.size()));
            Date date = new Date();
            if (date.compareTo(platformTrains[counter] .getArrival()) >= 0) {
                platformTrains[counter].setDelay((date.getTime()-platformTrains[counter] .getArrival().getTime())/1000);
                logger.info("chosen train: {}", platformTrains[counter] .getType() + platformTrains[counter] .getNumber());
                amqpTemplate.convertAndSend("arrival", "arrival.platform"+(counter+1), platformTrains[counter] );
                generatedTrains.remove(platformTrains[counter] .getType() + platformTrains[counter] .getNumber());
            } else {
                platformTrains[counter] = null;
            }


        } else {
            Date date = new Date();
            if (platformTrains[counter]  != null && platformTrains[counter] .getDeparture().compareTo(date) <= 0) {
                platformTrains[counter] .setDelay(date.getTime()-platformTrains[counter] .getDeparture().getTime());
                amqpTemplate.convertAndSend("departure", "departure."+(counter+1), platformTrains[counter] );
                platformTrains[counter]  = null;
            }
        }
        counter+=1;
        if (counter==platformTrains.length){
            counter=0;
        }

    }

//    @Scheduled(fixedRate = 10000, initialDelay = 12000)
//    public void platform2ServiceArrival() {
//
//        if (platform2Train == null && generatedTrains.size() > 0) {
//
//            List<Train> valuesList = new ArrayList<>(generatedTrains.values());
//            platform2Train = valuesList.get((int) (Math.random() * valuesList.size()));
//            Date date = new Date();
//            if (date.compareTo(platform2Train.getArrival()) >= 0) {
//                platform2Train.setDelay(date.getTime()-platform2Train.getArrival().getTime());
//                logger.info("chosen train: {}", platform2Train.getType() + platform2Train.getNumber());
//                amqpTemplate.convertAndSend("arrival", "arrival.platform2", platform2Train);
//                generatedTrains.remove(platform2Train.getType() + platform2Train.getNumber());
//            } else {
//                platform2Train = null;
//            }
//
//
//        } else {
//            Date date = new Date();
//            if (platform2Train != null && platform2Train.getDeparture().compareTo(date) <= 0) {
//                platform2Train.setDelay(date.getTime()-platform2Train.getDeparture().getTime());
//                amqpTemplate.convertAndSend("departure", "departure.2", platform2Train);
//                platform2Train = null;
//            }
//        }
//
//    }
//
//    @Scheduled(fixedRate = 10000, initialDelay = 14000)
//    public void platform3ServiceArrival() {
//        if (platform3Train == null && generatedTrains.size() > 0) {
//
//            List<Train> valuesList = new ArrayList<>(generatedTrains.values());
//            platform3Train = valuesList.get((int) (Math.random() * valuesList.size()));
//            Date date = new Date();
//            if (date.compareTo(platform3Train.getArrival()) >= 0) {
//                platform3Train.setDelay(date.getTime()-platform3Train.getArrival().getTime());
//                logger.info("chosen train: {}", platform3Train.getType() + platform3Train.getNumber());
//                amqpTemplate.convertAndSend("arrival", "arrival.platform3", platform3Train);
//                generatedTrains.remove(platform3Train.getType() + platform3Train.getNumber());
//            } else {
//                platform3Train = null;
//            }
//
//        } else {
//            Date date = new Date();
//            if (platform3Train != null && platform3Train.getDeparture().compareTo(date) <= 0) {
//                platform3Train.setDelay(date.getTime()-platform3Train.getDeparture().getTime());
//                amqpTemplate.convertAndSend("departure", "departure.3", platform3Train);
//                platform3Train = null;
//            }
//        }
//
//    }
//
//    @Scheduled(fixedRate = 10000, initialDelay = 16000)
//    public void platform4ServiceArrival() {
//
//        if (platform4Train == null && generatedTrains.size() > 0) {
//
//            List<Train> valuesList = new ArrayList<>(generatedTrains.values());
//            platform4Train = valuesList.get((int) (Math.random() * valuesList.size()));
//            Date date = new Date();
//            if (date.compareTo(platform4Train.getArrival()) >= 0) {
//                platform4Train.setDelay(date.getTime()-platform4Train.getArrival().getTime());
//                logger.info("chosen train: {}", platform4Train.getType() + platform4Train.getNumber());
//                amqpTemplate.convertAndSend("arrival", "arrival.platform4", platform4Train);
//                generatedTrains.remove(platform4Train.getType() + platform4Train.getNumber());
//            } else {
//                platform4Train = null;
//            }
//
//
//        } else {
//            Date date = new Date();
//            if (platform4Train != null && platform4Train.getDeparture().compareTo(date) <= 0) {
//                platform4Train.setDelay(date.getTime()-platform4Train.getDeparture().getTime());
//                amqpTemplate.convertAndSend("departure", "departure.4", platform4Train);
//                platform4Train = null;
//            }
//
//        }
//    }
//
//    @Scheduled(fixedRate = 10000, initialDelay = 11800)
//    public void platform5ServiceArrival() {
//
//        if (platform5Train == null && generatedTrains.size() > 0) {
//
//            List<Train> valuesList = new ArrayList<>(generatedTrains.values());
//            platform5Train = valuesList.get((int) (Math.random() * valuesList.size()));
//            Date date = new Date();
//            if (date.compareTo(platform5Train.getArrival()) >= 0) {
//                platform5Train.setDelay(date.getTime()-platform5Train.getArrival().getTime());
//                logger.info("chosen train: {}", platform5Train.getType() + platform5Train.getNumber());
//                amqpTemplate.convertAndSend("arrival", "arrival.platform5", platform5Train);
//                generatedTrains.remove(platform5Train.getType() + platform5Train.getNumber());
//            } else {
//                platform5Train = null;
//            }
//
//        } else {
//            Date date = new Date();
//            if (platform5Train != null && platform5Train.getDeparture().compareTo(date) <= 0) {
//                platform5Train.setDelay(date.getTime()-platform5Train.getDeparture().getTime());
//                amqpTemplate.convertAndSend("departure", "departure.5", platform5Train);
//                platform5Train = null;
//            }
//        }
//
//    }

}
