package com.bargain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BargainBackendApplication {
    public static void main(String[] args) {
        System.out.println("Starting Bargain Here Java Spring Boot Backend...");
        SpringApplication.run(BargainBackendApplication.class, args);
    }
}
