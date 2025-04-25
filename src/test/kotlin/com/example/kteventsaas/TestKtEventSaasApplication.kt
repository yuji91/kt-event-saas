package com.example.kteventsaas

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<KtEventSaasApplication>().with(TestcontainersConfiguration::class).run(*args)
}
