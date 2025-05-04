package com.example.kteventsaas.domain.tenant.repository

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import java.util.UUID

/**
 * テナントの永続化操作を定義するドメイン層のリポジトリインターフェース
 *
 * ---
 * 【役割】
 * - アプリケーション層やドメインサービスからテナント情報の保存・検索を行うための抽象的な契約。
 *
 * 【責務】
 * - 実際の永続化手段（JPA, RDB, 外部API など）に依存せず、
 *   テスト可能性・移植性・モジュール性の高いアーキテクチャを支える。
 *
 * 【設計意図】
 * - このインターフェースはドメイン層に位置し、インフラ層の実装に依存しない。
 * - Spring Data JPA や PostgreSQL などの実装は別途 `TenantJpaRepository` などで定義し、
 *   DIにより差し替え可能な構成とする。
 */
interface TenantRepository {

    fun save(tenant: Tenant): Tenant

    fun findById(id: UUID): Tenant?

    fun findByName(name: TenantName): Tenant?

    fun findAll(): List<Tenant>
}
