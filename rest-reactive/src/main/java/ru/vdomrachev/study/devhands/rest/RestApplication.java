package ru.vdomrachev.study.devhands.rest;

import jakarta.annotation.PostConstruct;
import reactor.blockhound.BlockHound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class RestApplication {

	// In your main class
	@PostConstruct
	public void init() {
		BlockHound.install();
	}

	public static void main(String[] args) {
		SpringApplication.run(RestApplication.class, args);
	}

}
