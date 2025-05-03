package com.example.kteventsaas.integration.config

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
object TestContainersConfig {

    @Container
    val postgres = PostgreSQLContainer("postgres:15").apply {
        withDatabaseName("test_db")
        withUsername("test_user")
        withPassword("test_pass")
    }

    class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(context: ConfigurableApplicationContext) {
            postgres.start()
            TestPropertyValues.of(
                "spring.datasource.url=${postgres.jdbcUrl}",
                "spring.datasource.username=${postgres.username}",
                "spring.datasource.password=${postgres.password}"
            ).applyTo(context.environment)
        }
    }
}
