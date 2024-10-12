package com.igrowker.nativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class NativoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NativoApplication.class, args);
	}

}
