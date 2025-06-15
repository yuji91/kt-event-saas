package com.example.kteventsaas.infrastructure.persistence.customer

import com.example.kteventsaas.domain.customer.entity.Customer
import com.example.kteventsaas.domain.customer.repository.CustomerRepository
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.customer.entity.CustomerJpaEntity
import com.example.kteventsaas.infrastructure.persistence.customer.mapper.CustomerMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * ドメイン層の `CustomerRepository` を JPA + Spring Data を用いて実装するインフラ層の具象リポジトリ
 *
 * ---
 * 【役割】
 * - `CustomerJpaEntity` による永続化操作をラップし、ドメイン層と DB アクセスの橋渡しを行う。
 *
 * 【責務】
 * - ドメインエンティティを `CustomerMapper` で `JpaEntity` に変換し、Spring Data リポジトリに委譲して保存・検索する。
 * - `EmailAddress` などの VO を JPA 変換可能な構造に対応させ、型安全なデータ処理を保証する。
 *
 * 【補足】
 * - `EmailAddress` は `@Converter` により DB カラムと相互変換可能な値オブジェクトである前提。
 * - インターフェースのみ定義すれば、Spring Boot 起動時に実装が自動生成される。
 *
 * 【注意】
 * - 本リポジトリはあくまで永続層（Infrastructure）であり、ドメイン層からは `CustomerRepository` 経由で利用する。
 */
@Repository
class CustomerJpaRepository(
    private val springDataRepository: CustomerSpringDataRepository,
    private val mapper: CustomerMapper
) : CustomerRepository {

    override fun save(customer: Customer): Customer {
        val entity: CustomerJpaEntity = mapper.toEntity(customer)
        val saved: CustomerJpaEntity = springDataRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun findByEmail(email: EmailAddress): Customer? {
        return springDataRepository.findByEmail(email)
            ?.let(mapper::toDomain)
    }

    override fun findById(id: UUID): Customer? {
        return springDataRepository.findByIdOrNull(id)
            ?.let(mapper::toDomain)
    }
}
