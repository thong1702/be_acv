package com.example.acv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcvApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcvApplication.class, args);
	}

}
