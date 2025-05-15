package com.example.kteventsaas.integration.repository

import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.domain.organizer.repository.OrganizerRepository
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.TenantJpaRepository
import com.example.kteventsaas.integration.BaseIntegrationTest
import org.assertj.core.api.Assertions.*
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import java.util.UUID

/**
 * ===============================================================
 * OrganizerRepositoryIntegrationTest
 * ---------------------------------------------------------------
 * Repository（永続化層）である OrganizerRepository の統合テスト
 *
 * ■ このテストの責務（WHAT）
 * - `save`, `findById`, `findByEmail` の各メソッドが正しく動作することを確認
 * - VO（EmailAddress, Role）のConverter/EnumType設定を検証
 * - Flyway マイグレーションによるスキーマ初期化を副次的確認
 * - DBのUNIQUE制約（tenant_id + email）で重複登録例外を検出
 *
 * ■ テスト設計方針（HOW）
 * - `BaseIntegrationTest` を継承し、Testcontainers + Flyway による実DB環境で検証
 * - 各テスト前に Flyway.clean() → Flyway.migrate() で副作用排除
 * - `usingRecursiveComparison()` で全フィールド一致を検証
 * - null制約テストはKotlin型で保証されるため `@Disabled`
 *
 * ■ テスト対象外（NOT covered）
 * - Service/Controller層のロジック
 * - DTO/GraphQL層の検証
 * - Kotlin型で到達不能なnull制約
 * ===============================================================
 */
class OrganizerRepositoryIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var tenantJpaRepository: TenantJpaRepository

    @Autowired
    lateinit var organizerRepository: OrganizerRepository

    @Autowired
    lateinit var flyway: Flyway

    @BeforeEach
    fun resetDatabase() {
        flyway.clean()
        flyway.migrate()
    }

    @DisplayName("観点: saveとfindByIdで保存・再取得が正しく行われ、全フィールドが一致すること")
    @Test
    fun `save and findById should persist organizer and retrieve it`() {
        // Arrange
        // まずはテナントを作成／取得して、そのIDを使う
        val tenant = Tenant(name = TenantName("TestTenant"))
        /* TenantRepositoryなどを使ってテスト用テナントを作成 */
        val savedTenant = tenantJpaRepository.save(tenant)
        // savedTenant.id がプラットフォーム型や Nullable 型で渡せない場合
        val tenantId: UUID = requireNotNull(savedTenant.id)  // ここで非 null を明示する

        val other = Organizer(tenantId = tenantId, email = EmailAddress("other@example.com"), passwordDigest = "pwd", role = Organizer.Role.OWNER)
        organizerRepository.save(other)
        val target = Organizer(tenantId = tenantId, email = EmailAddress("test@example.com"), passwordDigest = "secret", role = Organizer.Role.OWNER)

        // Act
        val saved = organizerRepository.save(target)
        val found = organizerRepository.findById(saved.id!!)

        // Assert
        assertThat(found).isNotNull
        assertThat(found!!.id).isEqualTo(saved.id)
        assertThat(found.email).isEqualTo(target.email)
        assertThat(found.role).isEqualTo(target.role)
        assertThat(found).usingRecursiveComparison().isEqualTo(saved)
    }

    @DisplayName("観点: findByEmailが正しくEmailAddressで該当データを取得できること")
    @Test
    fun `findByEmail should return correct organizer`() {
        // Arrange
        val tenant = Tenant(name = TenantName("TestTenant"))
        val savedTenant = tenantJpaRepository.save(tenant)
        val tenantId: UUID = requireNotNull(savedTenant.id)  // 非 null を明示した TenantID を用意

        val email = EmailAddress("organizer@example.com")
        organizerRepository.save(Organizer(tenantId = tenantId, email = email, passwordDigest = "pwd", role = Organizer.Role.OWNER))

        // Act
        val found = organizerRepository.findByEmail(email)

        // Assert
        assertThat(found).isNotNull
        assertThat(found!!.email).isEqualTo(email)
    }

    @DisplayName("観点: findByIdで存在しないIDを指定した場合、nullが返されること")
    @Test
    fun `findById should return null when organizer does not exist`() {
        val nonExistent = UUID.randomUUID()
        val result = organizerRepository.findById(nonExistent)
        assertThat(result).isNull()
    }

    @DisplayName("観点: findByEmailで存在しないEmailを指定した場合、nullが返されること")
    @Test
    fun `findByEmail should return null when organizer does not exist`() {
        val result = organizerRepository.findByEmail(EmailAddress("none@example.com"))
        assertThat(result).isNull()
    }

    @DisplayName("観点: 重複したEmailを保存しようとするとDB制約エラーが発生すること")
    @Test
    fun `saving duplicate email should raise exception`() {
        val tenant = Tenant(name = TenantName("TestTenant"))
        val savedTenant = tenantJpaRepository.save(tenant)
        val tenantId: UUID = requireNotNull(savedTenant.id)  // 非 null を明示した TenantID を用意

        val email = EmailAddress("dup@example.com")
        organizerRepository.save(Organizer(tenantId = tenantId, email = email, passwordDigest = "pwd", role = Organizer.Role.OWNER))

        assertThatThrownBy {
            organizerRepository.save(Organizer(tenantId = tenantId, email = email, passwordDigest = "pwd", role = Organizer.Role.OWNER))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Disabled("Kotlinの型でnullを防止しているため、本テストは不要")
    @DisplayName("観点: nullのEmailで保存しようとするとDB制約エラーが発生すること")
    @Test
    fun `saving null email should raise exception`() {
        // TODO: EntityManager 経由でテスト
    }
}
