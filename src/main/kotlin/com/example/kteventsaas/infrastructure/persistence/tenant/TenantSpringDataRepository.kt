package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

import java.util.UUID

/**
 * Spring Data JPA 用インタフェース
 * JpaRepository でカバーできる標準メソッド以外の内容を定義する
 *
 * findByName ではドメイン VO の value (String) を使うように定義しておくのが一般的だが、
 * Spring Data JPA の自動クエリ生成の仕組み上、Spring内部で型不一致エラーになるためVOで定義
 */
interface TenantSpringDataRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByName(name: TenantName): TenantJpaEntity?
}
