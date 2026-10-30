package com.aicloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProxyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProxyAiApplication.class, args);
    }
}
