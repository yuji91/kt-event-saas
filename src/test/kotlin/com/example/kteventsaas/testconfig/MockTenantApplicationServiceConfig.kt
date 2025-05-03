package com.example.kteventsaas.testconfig

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

// モック化された依存コンポーネントを Spring コンテナに明示的に登録
@TestConfiguration
class MockTenantApplicationServiceConfig {

    @Bean
    fun tenantApplicationService(): TenantApplicationService = mockk(relaxed = true) // 自動的にデフォルト値を返す
}