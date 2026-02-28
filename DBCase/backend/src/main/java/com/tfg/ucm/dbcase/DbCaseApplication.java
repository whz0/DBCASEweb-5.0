package com.tfg.ucm.dbcase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tfg.ucm.dbcase")
public class DbCaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(DbCaseApplication.class, args);
	}

}