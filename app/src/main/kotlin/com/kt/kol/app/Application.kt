package com.kt.kol.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@ComponentScan(basePackages = ["com.kt.kol"])
@EnableR2dbcRepositories(basePackages = ["com.kt.kol.api.**.repository"])
class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}