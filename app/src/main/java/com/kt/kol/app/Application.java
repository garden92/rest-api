package com.kt.kol.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = { "com.kt.kol" })
@ComponentScan(basePackages = { "com.kt.kol" })
@EnableScheduling
@EnableR2dbcRepositories(basePackages = "com.kt.kol.api.prechk.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
