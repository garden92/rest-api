package com.kt.kol.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {
	
	@Value("${e2e.properties.path:./config}")
	private String e2ePath;
	
	@Bean
	public ApplicationRunner applicationRunner() {
		return args -> {
			System.setProperty("e2e.properties.path", e2ePath);	//BMON용 e2e Path 설정
		};
	}
	
}
