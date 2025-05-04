package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

import java.util.UUID

/**
 * `TenantJpaEntity` に対する Spring Data JPA の永続化用リポジトリ
 *
 * ---
 * 【役割】
 * - JPAエンティティに対する基本的な CRUD 操作に加え、
 *   テナント名による検索（`findByName`）を提供する。
 * - Spring Data によって実装が自動生成され、`TenantJpaRepository` から委譲される内部構造。
 *
 * 【責務】
 * - ドメインとの変換責務やユースケース知識は一切持たず、純粋に DB アクセスのみを担当。
 *
 * 【補足】
 * - Spring Data JPAの自動クエリ生成では、
 *   前提として Repository メソッドの引数型と、エンティティのプロパティ型が完全一致している必要がある。
 *
 * - たとえエンティティ側で @Converter を設定していても、 Repositoryメソッドのパラメータ解析時には無視されるため、
 *   `findByName` の引数は VOの内部値（String）ではなく、VO型（`TenantName`）をそのまま使う必要がある。
 *
 * - そのため、VOのvalue（String）ではなく、VO型（TenantName）で定義する。
 * （@Query アノテーションを使えば型不一致も回避可能だが、今回は自動クエリ生成に準拠している）
 */
interface TenantSpringDataRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByName(name: TenantName): TenantJpaEntity?
}
