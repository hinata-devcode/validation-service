package com.venky.validationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ValidationserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ValidationserviceApplication.class, args);
	}

}
