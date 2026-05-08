package com.tfg.ucm.dbcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.tfg.ucm.dbcase")
@EnableScheduling
public class DbCaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbCaseApplication.class, args);
    }
}
