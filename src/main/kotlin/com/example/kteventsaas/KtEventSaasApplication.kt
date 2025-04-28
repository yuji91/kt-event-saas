package com.example.kteventsaas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class KtEventSaasApplication

fun main(args: Array<String>) {
	runApplication<KtEventSaasApplication>(*args)
}
