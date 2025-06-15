package com.example.kteventsaas.integration

import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

/**
 * ===============================================================
 * TestKtEventSaasIntegrationTest
 * ---------------------------------------------------------------
 * アプリケーション全体の起動確認を目的とした最小構成の統合テスト
 *
 * ■ このテストの責務（WHAT）
 * - Spring Boot アプリケーションがコンテキストエラーなく起動することの確認
 * - `@SpringBootTest` により、全構成（Bean定義, DI, Web環境）に問題がないことを検証
 * - `/actuator/health` エンドポイントにアクセスし、起動状態が "UP" であることを確認
 * - 本番プロファイル・本番相当構成（DB接続含む）でも最低限の起動が保証されているかを担保
 *
 * ■ テスト設計方針（HOW）
 * - MockMvc を使用し、HTTPリクエストベースで `/actuator/health` のレスポンスを検証
 * - Spring の `ApplicationContext` が正常にインスタンス化されているかを `assertThat` で確認
 * - 実際のアプリケーション構成に近い状態での smoke test（起動検知テスト）として運用
 *
 * ■ テスト対象外の責務（NOT covered）
 * - ControllerやServiceのビジネスロジックの検証
 * - 永続化やConverterなどの構成の正しさ（別の統合テストで担保）
 * - DBマイグレーションやトランザクションの妥当性確認
 *
 * ■ 使用技術・ライブラリ
 * - Spring Boot（@SpringBootTest によるコンテキスト起動）
 * - MockMvc（HTTPベースのエンドポイント検証）
 * - Actuator（ヘルスチェックエンドポイントの確認）
 * - AssertJ（ApplicationContextのnullチェック）
 *
 * ■ 観点
 * - ApplicationContext が起動できるか（構成エラーがないか）
 * - /actuator/health から "UP" を返すか（本番起動互換確認）
 *
 * ===============================================================
 */

@SpringBootTest
@AutoConfigureMockMvc
class TestKtEventSaasIntegrationTest {

    @Autowired
    lateinit var context: ApplicationContext

    @Autowired
    lateinit var mockMvc: MockMvc

    // application context loads（最小保証）
    @Test
    fun `application context loads`() {
        assertThat(context).isNotNull
    }

    // actuator/health テスト（本番互換の起動確認）
    @Test
    fun `GET actuator health should return 200 OK`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.status").value("UP")
            }
    }
}
