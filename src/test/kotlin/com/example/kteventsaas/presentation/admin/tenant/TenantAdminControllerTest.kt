package com.example.kteventsaas.presentation.admin.tenant

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.testconfig.MockTenantApplicationServiceConfig
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.verify
import io.mockk.Called
import io.mockk.clearMocks
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.server.ResponseStatusException
import java.util.*

/**
 * ===============================================================
 * TenantAdminControllerTest
 * ---------------------------------------------------------------
 * 管理画面向け TenantAdminController の単体テスト
 *
 * ■ このテストの責務（WHAT）
 * - HTTP リクエストに対して適切なレスポンス（ステータス・本文）を返すことを確認する
 * - サービス層の呼び出しが期待通りに行われる（回数・引数）ことを確認する
 * - 異常系（バリデーションエラー、重複、存在しないIDなど）に対して正しく例外ハンドリングされること
 * - REST API の仕様（エンドポイント、ステータスコード、レスポンス構造）のリグレッション検出
 *
 * ■ テスト対象外の責務（NOT covered）
 * - サービス層のビジネスロジックの正当性（例：永続化、重複チェック、整合性検証など）
 * - DTOの構造や Jackson によるシリアライズ／デシリアライズの妥当性（例：TenantResponse の形式）
 * - JSON構造の詳細（キー名やネスト構造）やグローバル例外ハンドラの出力フォーマット
 * - Spring Bootアプリケーション全体の構成や設定（Bean定義、DB接続など）
 *
 * ■ テスト設計方針（HOW）
 * - MockK による Stub（戻り値固定）と Mock（呼び出し検証）の併用で外部依存を排除
 * - `MockMvc` による HTTP レベルでのエンドポイントの振る舞い検証
 *  * - レスポンス内容の検証は JsonPath を使用し、DTOとの依存を排除（オブジェクト変換なし）
 * - 正常系・異常系・境界値ケースを分離し、観点ベースで明確にテスト分割
 * - 必要に応じて Jackson の ObjectMapper を使った JSON 生成は行うが、出力検証には用いない
 * - 必要があれば MockK による spy slot confirmVerified も追加する
 *
 * ■ 使用技術・ライブラリ
 * - Spring Boot（@SpringBootTest + MockMvc）
 * - MockK（every, verify, wasNot Called）
 * - Jackson（ObjectMapper）
 * - AssertJ（assertThat）
 * - JsonPath（レスポンス検証用）
 *
 * ■ 観点
 * - HTTP ステータスコード
 * - エラーメッセージ・例外型
 * - サービス呼び出し有無・引数の妥当性
 * - 境界値での仕様遵守（例：名前255文字まで許容など）
 * ===============================================================
 */
// @WebMvcTest(TenantAdminController::class) を使っていたが、以下に置き換えた
// @WebMvcTest は @EnableJpaAuditing などが有効になっていると、Entityがないために失敗する
// それを防ぐには excludeAutoConfiguration で DataJpa などを切る必要があるが、構成が複雑化しがち
// 一方、@SpringBootTest なら、必要なBeanもちゃんと生成されるので簡潔に収まる
@SpringBootTest
//@WebMvcTest(TenantAdminController::class)
@AutoConfigureMockMvc
@Import(MockTenantApplicationServiceConfig::class)
class TenantAdminControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var tenantApplicationService: TenantApplicationService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Nested
    inner class CreateTenant {


        // mockk()で作成したインスタンスでは verify や every などの呼び出し履歴がグローバルに保持されるため、全体実行時に各テスト実行前にクリアするよう定義
        @BeforeEach
        fun resetMocks() {
            clearMocks(tenantApplicationService)
        }

        // region Normal Cases - 正常系

        @DisplayName("観点: 正常な入力時に 201 Created と期待される JSON が返ること")
        @Test
        fun `should return 201 Created when request is valid`() {

            // --- Arrange ---
            val name = "ExampleTenant_01"
            val expectedId = UUID.fromString("11111111-2222-3333-4444-555555555555")

            // Stub : 「呼び出しに対して固定のレスポンスを返すダミー」
            // テスト時に外部依存（DB や他層のロジック）を切り離し、コントローラの振る舞いだけを検証できるようにします
            // 実際の処理（永続化やバリデーション）が走らないように“置き換える”
            every { tenantApplicationService.createTenant(name) }
                .returns(Tenant(expectedId, TenantName(name)))

            val requestBody = objectMapper.writeValueAsString(CreateTenantRequest(name))

            // --- Act & Assert ---
            mockMvc.post("/admin/tenants") {
                // リクエストヘッダーに Content-Type: application/json を付与
                // Spring Boot では JSON の入出力に MappingJackson2HttpMessageConverter（Jacksonベースのメッセージコンバータ）
                // 省略すると Content-Type ヘッダーが空またはデフォルト（text/plain 相当）になり、JSON コンバータが選ばれない
                contentType = MediaType.APPLICATION_JSON // JSON コンバータを選択させる
                content = requestBody // 実際の JSON ペイロードを渡す
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id").value(expectedId.toString())
                jsonPath("$.name").value(name)
            }

            // Mock : 「呼び出しの有無や引数を検証するダミー」
            //        呼び出しタイミングやパラメータが正しいかを断言できます
            // --- Verify service invocation ---
            verify { tenantApplicationService.createTenant(name) }
        }

        // endregion

        // region Exceptional Cases - 異常系

        @DisplayName("観点: name が空のときに 400 BadRequest + 適切なエラーメッセージが返ること")
        @Test
        fun `should return 400 BadRequest when name is blank`() {
            // TODO: GlobalExceptionHandler（@ControllerAdvice）移行時に修正

            // --- Arrange ---
            val request = CreateTenantRequest("")
            val body = objectMapper.writeValueAsString(request)

            // --- Act: 400 を返すことだけ検証して ResultActions を取得 ---
            val result = mockMvc.post("/admin/tenants") {
                contentType = MediaType.APPLICATION_JSON
                content     = body
            }
            .andExpect { status { isBadRequest() } }
            .andReturn()

            // --- Assert: 起きた例外の中身を検証 ---
            val ex = result.resolvedException as MethodArgumentNotValidException
            val fieldErr = ex.bindingResult.fieldError
            assertThat(fieldErr?.field).isEqualTo("name")
            assertThat(fieldErr?.defaultMessage).isEqualTo("Tenant name must not be blank")

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService wasNot Called }
        }

        // 確認観点 : 入力バリデーションと例外スローの挙動
        // Controller が例外を投げた時点でサービスを呼ばないため
        // 目的：境界値を渡したときに、想定通りの TenantResponse が返ってくるかを検証したい（値の詳細を見る
        // objectMapper.readValue で このテストの関心事は「長さ255が許容され、DTOの内容も正しいか」という具体値
        @DisplayName("観点: name が256文字のときに 400 BadRequest + バリデーションエラーが返ること")
        @Test
        fun `should return 400 BadRequest when name exceeds 255 characters`() {
            // TODO: GlobalExceptionHandler（@ControllerAdvice）移行時に修正

            // --- Arrange ---
            val longName = "a".repeat(256)
            val request  = CreateTenantRequest(longName)
            val body     = objectMapper.writeValueAsString(request)

            // --- Act: 400 を返すことだけ検証して ResultActions を取得 ---
            val result = mockMvc.post("/admin/tenants") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
            .andExpect { status { isBadRequest() } }
            .andReturn()

            // --- Assert: 例外オブジェクト検証 ---
            val ex = result.resolvedException as MethodArgumentNotValidException
            // 複数エラーがある場合は全体を取得しても OK
            val messages = ex.bindingResult.fieldErrors
                .filter { it.field == "name" }
                .map { it.defaultMessage }

            assertThat(messages).containsExactly("Tenant name must be 255 characters or less")

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService wasNot Called }
        }

        @DisplayName("観点: 重複した name の場合に 409 Conflict + エラーメッセージが返ること")
        @Test
        fun `should return 409 Conflict when name is duplicated`() {
            // TODO: GlobalExceptionHandler（@ControllerAdvice）移行時に修正

            // --- Arrange ---
            val duplicateName = "ExistingTenant"
            val request       = CreateTenantRequest(duplicateName)
            val body          = objectMapper.writeValueAsString(request)

            // サービスが HttpStatus.CONFLICT の例外を投げるようモック
            every {
                tenantApplicationService.createTenant(duplicateName)
            // MEMO: カスタム例外をまだ用意しないので、
            //       Spring がコントローラ層で自動的に 409 Conflict を返してくれる
            //       ResponseStatusException を利用
            } throws ResponseStatusException(
                HttpStatus.CONFLICT,
                "Tenant already exists"
            )

            // --- Act & Assert: 409 を返すことだけ検証して ResultActions を取得 ---
            val result = mockMvc.post("/admin/tenants") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }
            .andExpect { status { isConflict() } }
            .andReturn()

            // --- Assert: 例外オブジェクト検証 ---
            val ex = result.resolvedException as ResponseStatusException
            assertThat(ex.statusCode).isEqualTo(HttpStatus.CONFLICT)
            assertThat(ex.reason).isEqualTo("Tenant already exists")

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.createTenant(duplicateName) }
        }

        // endregion

        // region Boundary Cases - 境界値

        @DisplayName("観点: name が255文字のときに正常に作成できること")
        @Test
        fun `should create tenant when name length is exactly 255 characters`() {

            // --- Arrange ---
            val nameAtMaxLength = "a".repeat(255)
            val tenant = Tenant(UUID.randomUUID(), TenantName(nameAtMaxLength))
            every { tenantApplicationService.createTenant(nameAtMaxLength) } returns tenant
            val body = objectMapper.writeValueAsString(CreateTenantRequest(nameAtMaxLength))

            // --- Act & Assert ---
            mockMvc.post("/admin/tenants") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id").value(tenant.id.toString())
                jsonPath("$.name").value(nameAtMaxLength)
            }

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.createTenant(nameAtMaxLength) }
        }

        // endregion
    }

    @Nested
    inner class GetTenantById {

        // region Normal Cases - 正常系

        @DisplayName("観点: 指定した ID のテナントが存在する場合に 200 OK + 正しい JSON が返ること")
        @Test
        fun `should get tenant by id`() {

            // --- Arrange ---
            val tenantId = UUID.randomUUID()
            val tenantName = "ExampleTenant"
            val tenant = Tenant(tenantId, TenantName(tenantName))

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenant(tenantId) } returns tenant

            // --- Act & Assert ---
            mockMvc.get("/admin/tenants/$tenantId")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id").value(tenantId.toString())
                    jsonPath("$.name").value(tenantName)
                }

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.getTenant(tenantId) }
        }

        // endregion

        // region Exceptional Cases - 異常系

        // 目的：RESTのエラー応答仕様（errorCodeやmessage）を守っているかを検証。
        // JSONフォーマットが想定通りかを確認するケース
        @DisplayName("観点: 指定した ID のテナントが存在しない場合に 404 NotFound + エラー応答が返ること")
        @Test
        fun `should return 404 when tenant is not found by id`() {

            // --- Arrange ---
            val tenantId = UUID.randomUUID()

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenant(tenantId) } returns null

            // --- Act & Assert ---
            mockMvc.get("/admin/tenants/$tenantId")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.errorCode", `is`("TENANT_NOT_FOUND"))
                    jsonPath("$.message", `is`("Tenant not found"))
                }

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.getTenant(tenantId) }
        }

        // endregion
    }

    // MEMO: Controller テストでは「返却された内容が正しいか」のみを検証し、DTOの変換自体は責務外と切り分けるのがクリーンです。
    // TenantResponse.from() のテストはどこでやるべき？

    @Nested
    inner class GetTenantByName {

        // region Normal Cases - 正常系

        @DisplayName("観点: 指定した名前のテナントが存在する場合に 200 OK + 正しい JSON が返ること")
        @Test
        fun `should get tenant by name`() {

            // --- Arrange ---
            val name = "ExampleTenant_01"
            val tenant = Tenant(UUID.randomUUID(), TenantName(name))

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenantByName(TenantName(name)) } returns tenant

            // --- Act & Assert ---
            mockMvc.get("/admin/tenants/name/$name")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.id").value(tenant.id.toString())
                    jsonPath("$.name").value(name)
                }
        }


        // endregion

        // region Exceptional Cases - 異常系

        @DisplayName("観点: 指定した名前のテナントが存在しない場合に 404 NotFound + エラー応答が返ること")
        @Test
        fun `should return 404 when tenant is not found by name`() {
            val name = "NonExistentTenant"

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenantByName(TenantName(name)) } returns null

            mockMvc.get("/admin/tenants/name/$name")
                .andExpect {
                    status { isNotFound() }
                    jsonPath("$.errorCode", `is`("TENANT_NOT_FOUND"))
                    jsonPath("$.message", `is`("Tenant not found"))
                }
        }

        // endregion
    }

    @Nested
    inner class ListTenants {

        // region Normal Cases - 正常系

        @DisplayName("観点: テナントが存在する場合に 200 OK + リスト形式の JSON が返ること")
        @Test
        fun `should list all tenants`() {

            // --- Arrange ---
            val tenants = listOf(
                Tenant(UUID.randomUUID(), TenantName("T1")),
                Tenant(UUID.randomUUID(), TenantName("T2"))
            )
            // Return dummy data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.listTenants() } returns tenants

            // --- Act & Assert ---
            mockMvc.get("/admin/tenants")
                .andExpect {
                    status { isOk() }
                    jsonPath("$.length()").value(2)
                    jsonPath("$[0].id").value(tenants[0].id.toString())
                    jsonPath("$[0].name").value("T1")
                    jsonPath("$[1].id").value(tenants[1].id.toString())
                    jsonPath("$[1].name").value("T2")
                }
        }

        // endregion
    }
}