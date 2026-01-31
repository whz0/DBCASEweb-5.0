package com.tfg.ucm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class Application {

    /**
     * Application's main method
     *
     * @param args the console arguments
     */
    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }
}