package ru.angelika.boutique;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoutiqueApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoutiqueApplication.class, args);
	}

}