package com.example.formicarium_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.formicarium_app"})

public class FormicariumApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormicariumApplication.class, args);
	}
}