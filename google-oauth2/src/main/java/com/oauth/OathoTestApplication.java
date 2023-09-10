package com.oauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class OathoTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(OathoTestApplication.class, args);
	}

}
