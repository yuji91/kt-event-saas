package com.example.kteventsaas.integration.repository.tenant

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.integration.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

/**
 * ===============================================================
 * TenantRepositoryIntegrationTest
 * ---------------------------------------------------------------
 * Repository（永続化層）である TenantRepository の統合テスト
 *
 * ■ このテストの責務（WHAT）
 * - 実際のデータベースに対して永続化・取得が正しく行われることを確認する
 * - `save`, `findById`, `findByName` の各メソッドが、ドメイン層との整合性を保って動作することを確認
 * - `@Converter` などの ValueObject（TenantName）変換が正しく行われることを検証
 * - Flywayマイグレーションによってスキーマ初期化が正しく動作していることの副次的確認
 * - DBの一意制約（例: テナント名の重複）やNOT NULL制約の存在を確認し、マイグレーションの退行を検出する
 *
 * ■ テスト設計方針（HOW）
 * - `BaseIntegrationTest` を継承し、Testcontainers + Flyway による実DB環境での統合検証を行う
 * - テストごとに Flyway でスキーマを初期化し、副作用のないテストを保証
 * - `usingRecursiveComparison()` によって、全フィールド（ID・Name）の一致を検証
 * - ドメインオブジェクト（Entity, VO）単位でアサーションし、DTOには依存しない
 * - 制約テスト（重複登録・null保存）により、JPAアノテーションとDB制約が一致しているかを確認
 *
 * ■ テスト対象外の責務（NOT covered）
 * - Controller → Service への呼び出し（APIレイヤー）
 * - サービス層のビジネスロジックの分岐やバリデーション
 * - DTOやJSON形式の検証（これは Controller / Serializer 層の責務）
 * - データベースの接続設定やプロファイル切り替え
 * - Kotlinコードによって到達不能な `null` 制約の詳細確認（別途 EntityManager による検証が必要）
 *
 * ■ 使用技術・ライブラリ
 * - Spring Boot（@SpringBootTest）
 * - Testcontainers（PostgreSQL環境の一時起動）
 * - Flyway（スキーママイグレーション）
 * - AssertJ（assertThat, usingRecursiveComparison）
 *
 * ■ 観点
 * - save で ID が生成され、値が正しく保存されること
 * - findById / findByName で保存済みデータが取得できること
 * - 存在しないID/Nameに対して null を返すこと
 * - VO（TenantName）の変換処理がシリアライズ／デシリアライズを通じて破綻しないこと
 * - テナント名の一意制約（UNIQUE制約）がDB上で機能していること
 * - null禁止制約（NOT NULL）がKotlinの型と整合していること（型で保証済みのため一部無効化済）
 * ===============================================================
 */
class TenantRepositoryIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var tenantRepository: TenantRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun resetDatabase() {
        flyway.clean()
        flyway.migrate()
    }
    @DisplayName("観点: saveとfindByIdで保存・再取得が正しく行われ、全フィールドが一致すること")
    @Test
    fun `save and findById should persist tenant and retrieve it`() {
        // Arrange
        // 取得対象外のデータを用意
        tenantRepository.save(Tenant(name = TenantName("OtherTenant1")))
        tenantRepository.save(Tenant(name = TenantName("OtherTenant2")))
        // 取得対象のデータを用意
        val tenantName = TenantName("TestTenant")
        val tenant = Tenant(name = tenantName)

        // Act
        val saved = tenantRepository.save(tenant)
        val found = tenantRepository.findById(saved.id!!)

        // Assert
        assertThat(found).isNotNull
        assertThat(found!!.id).isEqualTo(saved.id)
        assertThat(found.name).isEqualTo(tenantName)
        assertThat(found).usingRecursiveComparison().isEqualTo(saved) // save() の戻り値と DB再取得値の全フィールド一致を検証
    }

    @DisplayName("観点: findByNameが正しくTenantNameを用いて該当データを取得できること")
    @Test
    fun `findByName should return correct tenant`() {
        // Arrange
        val tenantName = TenantName("AnotherTenant")
        tenantRepository.save(Tenant(name = tenantName))

        // Act
        val found = tenantRepository.findByName(tenantName)

        // Assert
        assertThat(found).isNotNull
        assertThat(found!!.name).isEqualTo(tenantName)
    }

    @DisplayName("観点: findByIdで存在しないIDを指定した場合、nullが返されること")
    @Test
    fun `findById should return null when tenant does not exist`() {
        val nonExistentId = UUID.randomUUID()
        val result = tenantRepository.findById(nonExistentId)
        assertThat(result).isNull()
    }

    @DisplayName("観点: findByNameで存在しない名前を指定した場合、nullが返されること")
    @Test
    fun `findByName should return null when tenant does not exist`() {
        val nonExistentName = TenantName("NonExistentTenant")
        val result = tenantRepository.findByName(nonExistentName)
        assertThat(result).isNull()
    }

    @DisplayName("観点: 重複したテナント名を保存しようとするとDB制約エラーが発生すること")
    @Test
    fun `saving duplicate tenant name should raise exception`() {
        val name = TenantName("DuplicateTenant")
        tenantRepository.save(Tenant(name = name))

        assertThatThrownBy {
            tenantRepository.save(Tenant(name = name))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

//    @Disabled("Kotlinの型でnullを防止しているため、本テストは不要")
//    @DisplayName("観点: nullのテナント名を保存しようとするとDB制約エラーが発生すること")
//    @Test
//    fun `saving null name should raise exception`() {
        // TODO: 確認する場合、@Transactional を使い、EntityManager 経由で保存してKotlinの型チェックを回避
        // TODO: EntityManager.flush() で制約違反を意図的に発生させて確認する
//        val invalidTenant = Tenant(name = null) // Kotlinでは null 非許容なら直接書けないが仮定

//        assertThatThrownBy {
//            tenantRepository.save(invalidTenant)
//        }.isInstanceOf(DataIntegrityViolationException::class.java)
//    }
}
