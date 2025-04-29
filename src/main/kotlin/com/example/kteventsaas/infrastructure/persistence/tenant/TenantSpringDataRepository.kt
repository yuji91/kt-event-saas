package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

import java.util.UUID

/**
 * Spring Data JPA 用インタフェース（JpaRepository の標準メソッド以外を定義）
 *
 * ※ Spring Data JPAの自動クエリ生成では、
 * Repositoryメソッドの引数型と、エンティティプロパティ型が完全一致していることが前提となる。
 *
 * たとえエンティティ側で @Converter を設定していても、
 * Repositoryメソッドのパラメータ解析時には無視されるため、
 * findBy系メソッドの引数には、エンティティのプロパティ型（ここではTenantName）をそのまま使う必要がある。
 *
 * そのため、VOのvalue（String）ではなく、VO型（TenantName）で定義する。
 * (@Queryアノテーションで別途JPQLクエリを記載すれば回避可能)
 */
interface TenantSpringDataRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByName(name: TenantName): TenantJpaEntity?
}
