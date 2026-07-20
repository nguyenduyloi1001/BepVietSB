package com.example.Bep_Viet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BepVietApplication {

	public static void main(String[] args) {
		SpringApplication.run(BepVietApplication.class, args);
	}

}
