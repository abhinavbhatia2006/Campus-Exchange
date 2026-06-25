package com.dormex.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DormexApplication {

	public static void main(String[] args) {
		SpringApplication.run(DormexApplication.class, args);
	}

}
