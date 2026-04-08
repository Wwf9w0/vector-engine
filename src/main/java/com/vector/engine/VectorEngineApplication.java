package com.vector.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan("com.vector.engine.config")
public class VectorEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(VectorEngineApplication.class, args);
	}

}
