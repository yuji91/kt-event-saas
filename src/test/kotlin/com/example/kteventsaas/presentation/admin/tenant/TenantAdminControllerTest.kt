package com.example.kteventsaas.presentation.admin.tenant

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import com.example.kteventsaas.testconfig.MockTenantApplicationServiceConfig
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.verify
import io.mockk.Called
import io.mockk.clearMocks
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
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

// テスト概要
// 「HTTP リクエストを受けて適切な例外・ステータスを返し、サービス呼び出しを正しく行う／行わない」
//
//という 外縁的な振る舞い にフォーカスし、内部のビジネスロジックや JSON シリアライズなどの詳細は切り落とす

// Stub : Controllerから見た外部依存の「振る舞いを固定」する
// 「この引数で呼ばれたらこの値を返す／この例外を投げる」という「入力→出力」を決めておく
//  例 : every { tenantApplicationService.createTenant(duplicateName) } throws ConflictException(...)
//  Stub だけだと「返す内容」は決められますが、その後呼ばれたかどうかまでは追えない

// Stub（返り値固定）

// Mock : テスト終了時に「その依存が期待どおり呼ばれたか」を検証
// 呼び出し回数や引数をチェックして、テスト対象の内部ロジックが正しく依存を使っているかを保証
// 例 : verify { tenantApplicationService.createTenant(duplicateName) }
//      confirmVerified(tenantApplicationService)
// Mock だけだと「呼ばれたか」は確認できるものの、返り値や例外をカスタマイズできないことが多い

// これらを踏まえて以下を同時にテスト
// ・コントローラ呼び出し時にサービス層がどう動くか（Stub）
// ・本当にサービス層を一度だけ／呼ぶべきタイミングで呼んでいるか（Mock）

// 責務（WHAT「何を検証するか」）
// 不正なリクエストで400/409など正しいステータスが返ること
// 例外オブジェクト（MethodArgumentNotValidException や ConflictException）の型やメッセージが正しいこと
// サービス呼び出しの有無・引数が想定どおりであること

// テスト責務を書き出すときは「何を確認するか（ステータス、例外、中身、サービス呼び出し）」にフォーカスし
// Mock／Stub はそれを達成するための実装上の方法として整理しておく

// 手段 HOW
// Stub：every { … } throws … で依存の振る舞いを固定し
// Mock：verify { … }／confirmVerified で呼び出しの有無・回数・引数を検証する

// 1. リクエスト→バリデーション挙動の検証
// @Valid が働き、入力が不正な場合に MethodArgumentNotValidException が発生すること
// 不正入力時に HTTP 400 が返されること

// 2. HTTP ステータスコードの検証
// 正常系（名前が空でない、かつ重複しない場合）は 201 に飛ぶ想定を別途テストする
// 異常系（空文字／文字数超過では 400、重複では 409 が返されること）

// 3. 例外オブジェクトの中身検証（B. 例外オブジェクト検証方式）
// resolvedException で取得した例外の型が想定どおりであること
// 例外メッセージやカスタムプロパティ（errorCode など）が正しいこと

// 4. サービス呼び出しの有無・呼び出しパラメータの検証
// バリデーション NG ケースでサービスが一切呼ばれないこと
// 正常／重複ケースでサービスの createTenant(name) が期待どおり呼ばれること

// 5. Mock／Stub の適切な使い分け
// Stub (every { … } throws …) でサービス層の例外スローを再現し
// Mock (verify { … } / confirmVerified) で呼び出し状況を検証

// Controller テストの「責務でない」内容
// 1. サービス層内部のロジック検証
// 重複チェックや永続化の具体的実装、DBトランザクションの挙動などはテストしない

// 2. グローバル例外ハンドラ（@ControllerAdvice）や JSON レスポンスフォーマットの詳細
// 今回は例外オブジェクト検証方式なので、JSON のキーや構造は責務外

// 3. DTO やエンティティの振る舞いテスト
// CreateTenantRequest 自体の getter/setter や TenantResponse.from(...) の変換ロジックは別の単体テスト領域

// 4. Spring Boot アプリケーション全体の起動やコンテキスト構成
// Bean 定義の有無、DataSource や JPA 設定の整合性などはコントローラテストでカバーしない


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

        // 目的：Controllerの「エンドポイントとしての振る舞い」を検証したい
        // Controllerの“入出力インタフェース”検証が責務。DTOの整合性は別テスト
        // Assert は jsonPath で HTTPの振る舞い確認に集中
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

        @Test
        fun `should create tenant when name length is exactly 255 characters`() {

            // --- Arrange ---
            val nameAtMaxLength = "a".repeat(255)
            val tenant = Tenant(UUID.randomUUID(), TenantName(nameAtMaxLength))
            every { tenantApplicationService.createTenant(nameAtMaxLength) } returns tenant
            val body = objectMapper.writeValueAsString(CreateTenantRequest(nameAtMaxLength))

            // --- Act & Assert
            val response = mockMvc.post("/admin/tenants") {
                contentType = MediaType.APPLICATION_JSON
                content = body
            }.andExpect {
                status { isCreated() }
            }.andReturn().response

            // デシリアライズすることで、単なる文字列ではなくKotlinオブジェクトとしてプロパティ単位でアクセスできるようにする
            val tenantResponse = objectMapper.readValue(response.contentAsString, TenantResponse::class.java)
            assertThat(tenantResponse.name).isEqualTo(nameAtMaxLength)
            assertThat(tenantResponse.id).isEqualTo(tenant.id)

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.createTenant(nameAtMaxLength) }
        }

        // endregion
    }

    @Nested
    inner class GetTenantById {

        // region Normal Cases - 正常系

        @Test
        fun `should get tenant by id`() {

            // --- Arrange ---
            val tenantId = UUID.randomUUID()
            val tenantName = "ExampleTenant"
            val tenant = Tenant(tenantId, TenantName(tenantName))

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenant(tenantId) } returns tenant

            // --- Act & Assert
            val response = mockMvc.get("/admin/tenants/$tenantId")
                .andExpect { status { isOk() } }
                .andReturn().response

            val tenantResponse = objectMapper.readValue(response.contentAsString, TenantResponse::class.java)

            assertThat(tenantResponse.id).isEqualTo(tenantId)
            assertThat(tenantResponse.name).isEqualTo(tenantName)

            // --- Verify service invocation (Mock) ---
            verify { tenantApplicationService.getTenant(tenantId) }
        }

        // endregion

        // region Exceptional Cases - 異常系

        // 目的：RESTのエラー応答仕様（errorCodeやmessage）を守っているかを検証。
        // JSONフォーマットが想定通りかを確認するケース
        @Test
        fun `should return 404 when tenant is not found by id`() {

            // --- Arrange ---
            val tenantId = UUID.randomUUID()

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenant(tenantId) } returns null

            // --- Act & Assert
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

        @Test
        fun `should get tenant by name`() {
            val name = "ExampleTenant_01"
            val tenant = Tenant(UUID.randomUUID(), TenantName(name))

            // Return dummy_data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.getTenantByName(TenantName(name)) } returns tenant

            val response = mockMvc.get("/admin/tenants/name/$name")
                .andExpect { status { isOk() } }
                .andReturn().response

            val tenantResponse = objectMapper.readValue(response.contentAsString, TenantResponse::class.java)

            assertThat(tenantResponse.name).isEqualTo(name)
            assertThat(tenantResponse.id).isEqualTo(tenant.id)
        }

        // endregion

        // region Exceptional Cases - 異常系

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

        @Test
        fun `should list all tenants`() {
            val tenants = listOf(
                Tenant(UUID.randomUUID(), TenantName("T1")),
                Tenant(UUID.randomUUID(), TenantName("T2"))
            )

            // Return dummy data and isolate this test from external dependencies (Stub)
            every { tenantApplicationService.listTenants() } returns tenants

            val response = mockMvc.get("/admin/tenants")
                .andExpect { status { isOk() } }
                .andReturn().response

            val tenantResponses: List<TenantResponse> = objectMapper.readValue(
                response.contentAsString,
                object : TypeReference<List<TenantResponse>>() {}
            )

            assertThat(tenantResponses).hasSize(2)
            assertThat(tenantResponses.map { it.name }).containsExactly("T1", "T2")
        }

        // endregion
    }
}