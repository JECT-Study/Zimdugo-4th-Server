package com.zimdugo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ZimdugoBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZimdugoBeApplication.class, args);
	}

}
