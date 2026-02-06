package com.tony.roadtrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RoadtripApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoadtripApplication.class, args);
	}

}
