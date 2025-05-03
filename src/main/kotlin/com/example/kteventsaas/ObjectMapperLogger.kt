package com.example.kteventsaas

// 以下は単なる 型のインポート（名前解決） に過ぎず、インスタンスの中身や初期化方針には一切影響を与えない
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class ObjectMapperLogger(
    private val objectMapper: ObjectMapper
) {
    @PostConstruct
    fun logObjectMapper() {
        println("✅ [確認] ObjectMapper instance: ${objectMapper::class.qualifiedName}")
        println("✅ [確認] Registered modules: ${objectMapper.registeredModuleIds}")
        println("✅ [確認] SerializationFeature.WRITE_DATES_AS_TIMESTAMPS: ${objectMapper.isEnabled(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)}")
    }
}
