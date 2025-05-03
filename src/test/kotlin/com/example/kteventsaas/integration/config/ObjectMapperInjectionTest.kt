package com.example.kteventsaas.integration.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ObjectMapperInjectionTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `ObjectMapper should be injected`() {
        assertNotNull(objectMapper, "ObjectMapper should have been autowired by Spring")
    }
}
