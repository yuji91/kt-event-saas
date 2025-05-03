package com.example.kteventsaas.integration

import com.example.kteventsaas.integration.config.TestContainersConfig
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

/**
 * 統合テスト用のベースクラス。
 * TestContainersConfig により PostgreSQL コンテナが起動され、Spring Boot コンテキストに接続される。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestContainersConfig.Initializer::class])
abstract class BaseIntegrationTest
