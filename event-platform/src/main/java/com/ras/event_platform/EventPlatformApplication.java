package com.ras.event_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventPlatformApplication.class, args);
	}

}
