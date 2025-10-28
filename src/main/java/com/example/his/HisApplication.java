package com.example.his;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HisApplication {
	public static void main(String[] args) {
		SpringApplication.run(HisApplication.class, args);
	}
}
