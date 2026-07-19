package com.cgp.controlgasto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ControlgastoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ControlgastoApplication.class, args);
	}

}
