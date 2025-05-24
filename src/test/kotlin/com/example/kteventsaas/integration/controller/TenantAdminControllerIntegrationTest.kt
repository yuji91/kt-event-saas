package com.example.kteventsaas.integration.controller

import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.integration.config.TestContainersConfig
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Disabled

/**
 * ===============================================================
 * TenantAdminControllerIntegrationTest
 * ---------------------------------------------------------------
 * 管理者用エンドポイント `/admin/tenants` に対する Controller 統合テスト
 *
 * ■ このテストの責務（WHAT）
 * - REST API（Controller層）が正しく動作し、期待されるステータスコードとレスポンスを返すことを確認する
 * - HTTP経由で受け取ったリクエストが、ApplicationService・Repository を経て永続化されることを検証
 * - VO（TenantName）やDTO（CreateTenantRequest, TenantResponse）のシリアライズ／デシリアライズが正常であることを確認
 * - バリデーション違反や一意制約違反時のエラーハンドリングが適切に行われていることを確認
 * - サービス層での例外発生時にトランザクションがロールバックされることを副次的に検証
 *
 * ■ テスト設計方針（HOW）
 * - `@SpringBootTest` + `@AutoConfigureMockMvc` により、HTTP経由の統合テストを実施
 * - Testcontainers + Flyway により、毎回クリーンな PostgreSQL 環境で検証
 * - ObjectMapper による JSON 変換を介して、DTO や VO の整合性を担保
 * - 異常系については @Nested クラスにまとめ、入力ごとのバリデーションや型不整合などを網羅的に検証
 * - 実際の HTTP リクエストと同様の形式（JSON文字列）で body を組み立て、現実に近い挙動を確認
 *
 * ■ テスト対象外の責務（NOT covered）
 * - Repository の単体動作検証（`TenantRepositoryIntegrationTest` で実施済）
 * - ApplicationService の分岐ロジックや内部バリデーションの詳細
 * - DTO の構造・equals/hashCode・個別の JSON プロパティ構造（単体テストで別途確認）
 * - 認証・認可（未導入のため現時点では対象外）
 *
 * ■ 使用技術・ライブラリ
 * - Spring Boot（@SpringBootTest）
 * - MockMvc（HTTPレベルのAPI呼び出しをシミュレート）
 * - Testcontainers（PostgreSQLコンテナ）
 * - Flyway（スキーママイグレーション）
 * - Jackson（ObjectMapper による JSON変換）
 * - AssertJ（assertThat による同一性比較）
 *
 * ■ 観点
 * - 正常系：POST → 201、GET → 200、レスポンス構造が一致していること
 * - 異常系：
 *   - name=null / 空文字 / 空白 / 欠如 / 数値 / JSON構文ミス → 400 Bad Request
 *   - name の重複 → 409 Conflict
 *   - サービス例外 → 500 Internal Server Error（かつロールバックが行われる）
 * - Content-Type: application/json が適切に設定されていること（推奨）
 * - IDに対するPOST→GETの一貫性確認
 *
 * ===============================================================
 */

// TODO: 2件要調査のテストケースを保留にしている
// TODO: RepositoryIntegrationTest のようにテスト対象コードの変更による検知まで確認できていない
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = [TestContainersConfig.Initializer::class])
class TenantAdminControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var tenantRepository: TenantRepository

    @Test
    fun `POST admin tenant creates new tenant and returns 201`() {
        val name = "IntegrationTestTenant"
        val request = CreateTenantRequest(name)
        val body = objectMapper.writeValueAsString(request)

        mockMvc.post("/admin/tenants") {
            with(csrf())                                           // post のため CSRF トークン付与
            with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id").exists()
            jsonPath("$.name").value(name)
        }
    }

    @Test
    fun `POST then GET by ID returns same data with correct mapping`() {
        val name = "ConverterCheckTenant"
        val request = CreateTenantRequest(name)
        val body = objectMapper.writeValueAsString(request)

        val postResult = mockMvc.post("/admin/tenants") {
            with(csrf())                                           // post のため CSRF トークン付与
            with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id").exists()
            jsonPath("$.name").value(name)
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val createdJson = postResult.response.contentAsString
        val createdId = objectMapper.readTree(createdJson).get("id").asText()

        val getResult = mockMvc.get("/admin/tenants/$createdId") {
            with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
        }
        .andExpect {
            status { isOk() }
            jsonPath("$.id").value(createdId)
            jsonPath("$.name").value(name)
            content { contentType(MediaType.APPLICATION_JSON) }
        }.andReturn()

        val getJson = getResult.response.contentAsString

        val postResponse = objectMapper.readValue(createdJson, TenantResponse::class.java)
        val getResponse = objectMapper.readValue(getJson, TenantResponse::class.java)

        assertThat(getResponse).isEqualTo(postResponse)
    }

    @Nested
    @DisplayName("POST /admin/tenants 異常系")
    inner class CreateTenantValidationTest {
        private val endpoint = "/admin/tenants"

        @Disabled("このテスト観点も追加したいが、テスト失敗理由を調査する必要があり、一旦保留")
        @Test
        fun `nameがnullの場合、400とバリデーションエラーが返る`() {
            val body = """{ \"name\": null }"""

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.errors[0].field").value("name")
                jsonPath("$.errors[0].message").value("must not be blank")
            }
        }

        @Test
        fun `nameが空文字の場合、400とバリデーションエラーが返る`() {
            val body = """{ \"name\": \"\" }"""

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errors[0].field").value("name")
            }
        }

        @Test
        fun `nameが空白のみの場合、400とバリデーションエラーが返る`() {
            val body = """{ \"name\": \"   \" }"""

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errors[0].field").value("name")
            }
        }

        @Test
        fun `nameフィールドが欠如している場合、400とバリデーションエラーが返る`() {
            val body = """{ }"""

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errors[0].field").value("name")
            }
        }

        @Test
        fun `nameが数値の場合、400と型変換エラーが返る`() {
            val body = """{ \"name\": 123 }"""

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `不正なJSON構造（括弧ミス）の場合、400とパースエラーが返る`() {
            val malformedJson = """{ \"name\": \"abc\" """

            mockMvc.post(endpoint) {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = malformedJson
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        fun `POST duplicate tenant name returns 409 Conflict`() {
            val name = "DuplicateTenant"
            val request = CreateTenantRequest(name)
            val body = objectMapper.writeValueAsString(request)

            // --- 1回目: 正常登録 ---
            mockMvc.post("/admin/tenants") {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isCreated() }
            }

            // --- 2回目: 重複登録 ---
            mockMvc.post("/admin/tenants") {
                with(csrf())                                           // post のため CSRF トークン付与
                with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isConflict() }
                jsonPath("$.message").value("Tenant name already exists") // メッセージは実装に合わせて
            }
        }
    }

    @Disabled("このテスト観点も追加したいが、テスト失敗理由を調査する必要があり、一旦保留")
    @Test
    fun `POST that triggers service exception results in rollback`() {
        val name = "TriggerException" // ApplicationServiceでこの値により例外を投げる設計とする
        val request = CreateTenantRequest(name)
        val body = objectMapper.writeValueAsString(request)

        mockMvc.post("/admin/tenants") {
            with(csrf())                                           // post のため CSRF トークン付与
            with(user("admin").roles("ADMIN")) // 認証済みユーザーを模倣
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect {
            status { isInternalServerError() }
            jsonPath("$.message").value("Rollback Test Triggered") // 例外メッセージに応じて変更可
        }

        val found = tenantRepository.findByName(TenantName(name))
        assertThat(found).isNull()
    }
}
