package com.github.mrchcat.front;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankFront {

    public static void main(String[] args) {
        SpringApplication.run(BankFront.class, args);
    }
}
