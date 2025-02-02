package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class KafkaBasic {
    public static void main(String[] args) {
        SpringApplication.run(KafkaBasic.class, args);

        log.info("Application started!");
    }
}