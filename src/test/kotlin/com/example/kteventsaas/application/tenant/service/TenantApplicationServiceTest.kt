package com.example.kteventsaas.application.tenant.service

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * ===============================================================
 * TenantApplicationServiceTest
 * ---------------------------------------------------------------
 * アプリケーションサービス TenantApplicationService の単体テスト
 *
 * ■ このテストの責務（WHAT）
 * - アプリケーションサービスの振る舞いの検証
 * - 正常系・異常系の入力に対し、期待される出力・振る舞いを返すこと
 * - レイヤー間の責務（ドメイン vs アプリケーション）が混在していないこと
 *
 * ■ テスト手段（HOW）
 * - Repository はモック化し、永続化層に依存しないこと
 * - Repository のメソッドが期待通り呼ばれたか verify で確認すること
 * - 返却されるオブジェクトの状態を Assert で検証すること
 *
 * ■ テスト対象外の責務（NOT covered）
 * - エンティティ (Tenant) の内部ロジック
 * （TenantName のバリデーションや、Tenant の整合性制約は ドメイン層のユニットテストで扱う）
 * - リポジトリの具体的な実装（DBアクセス）
 * （TenantRepository が正しくデータベースと通信しているかは リポジトリ層の統合テストで扱う）
 * - RESTコントローラ層との接続（APIインターフェース）
 * （TenantApplicationService を REST Controller がどう使っているかは、Web層の結合テストや @SpringBootTest で確認する）
 * - トランザクションや例外処理の詳細
 * （例外を投げる仕様であれば別途 throw をテストするが、このコードでは null を返しているため、例外処理テストは責務外）
 *
 * ■ テスト設計方針
 * - 正常系 → 異常系（null返却）を網羅
 * - Mockを使ってRepository依存を排除
 * - メソッド単位に @Nested クラスで整理
 *
 * ■ 使用ライブラリ
 * - MockK
 * - JUnit5
 * - AssertJ
 * ===============================================================
 */
@Tag("UnitTest")
class TenantApplicationServiceTest {

    private val tenantRepository: TenantRepository = mockk(relaxed = true)
    private val tenantApplicationService = TenantApplicationService(tenantRepository)

    @Nested
    inner class CreateTenant {

        // region Normal Cases - 正常系

        @Test
        fun `should create tenant successfully`() {
            val name = "NewTenant"
            val tenant = Tenant(UUID.randomUUID(), TenantName(name))

            every { tenantRepository.save(any()) } returns tenant

            val result = tenantApplicationService.createTenant(name)

            assertThat(result.name.value).isEqualTo(name)
            verify { tenantRepository.save(any()) }
        }

        // endregion
    }

    @Nested
    inner class GetTenant {

        // region Normal Cases - 正常系

        @Test
        fun `should return tenant when found by id`() {
            val id = UUID.randomUUID()
            val tenant = Tenant(id, TenantName("ExistingTenant"))

            every { tenantRepository.findById(id) } returns tenant

            val result = tenantApplicationService.getTenant(id)

            assertThat(result).isNotNull
            assertThat(result?.id).isEqualTo(id)
        }

        // endregion

        // region Exceptional Cases - 異常系

        @Test
        fun `should return null when tenant not found by id`() {
            val id = UUID.randomUUID()

            every { tenantRepository.findById(id) } returns null

            val result = tenantApplicationService.getTenant(id)

            assertThat(result).isNull()
        }

        // endregion
    }

    @Nested
    inner class GetTenantByName {

        // region Normal Cases - 正常系

        @Test
        fun `should return tenant when found by name`() {
            val name = TenantName("TenantByName")
            val tenant = Tenant(UUID.randomUUID(), name)

            every { tenantRepository.findByName(name) } returns tenant

            val result = tenantApplicationService.getTenantByName(name)

            assertThat(result).isNotNull
            assertThat(result?.name).isEqualTo(name)
        }

        // endregion

        // region Exceptional Cases - 異常系

        @Test
        fun `should return null when tenant not found by name`() {
            val name = TenantName("NonExistentTenant")

            every { tenantRepository.findByName(name) } returns null

            val result = tenantApplicationService.getTenantByName(name)

            assertThat(result).isNull()
        }

        // endregion
    }

    @Nested
    inner class ListTenants {

        // region Normal Cases - 正常系

        @Test
        fun `should return list of tenants`() {
            val tenants = listOf(
                Tenant(UUID.randomUUID(), TenantName("Tenant1")),
                Tenant(UUID.randomUUID(), TenantName("Tenant2"))
            )

            every { tenantRepository.findAll() } returns tenants

            val result = tenantApplicationService.listTenants()

            assertThat(result).hasSize(2)
            assertThat(result.map { it.name.value }).containsExactly("Tenant1", "Tenant2")
        }

        // endregion

        // region Boundary Cases - 境界値

        @Test
        fun `should return empty list when no tenants exist`() {
            every { tenantRepository.findAll() } returns emptyList()

            val result = tenantApplicationService.listTenants()

            assertThat(result).isEmpty()
        }

        // endregion
    }
}