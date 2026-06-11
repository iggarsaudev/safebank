package com.safebank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // ESTO ENCIENDE LOS CRON JOBS
public class SafeBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SafeBankApplication.class, args);
    }

}