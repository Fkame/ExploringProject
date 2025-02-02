package org.example.consumer;

import org.springframework.kafka.annotation.KafkaListener;

public class Topic1Listener {

    @KafkaListener(topics = "topic1")
    public void process(String key, String message) {

    }
}
