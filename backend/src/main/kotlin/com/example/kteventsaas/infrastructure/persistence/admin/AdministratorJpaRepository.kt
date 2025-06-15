package com.example.kteventsaas.infrastructure.persistence.admin

import com.example.kteventsaas.domain.admin.entity.Administrator
import com.example.kteventsaas.domain.admin.repository.AdministratorRepository
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.admin.mapper.AdministratorMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * ドメイン層の `AdministratorRepository` を JPA + Spring Data を用いて実装するインフラ層の具象リポジトリ
 *
 * ---
 * 【役割】
 * - `AdministratorJpaEntity` による永続化操作をラップし、ドメイン層とDBアクセスの橋渡しを行う。
 * - 技術的関心（JPA, Spring Data）をアプリケーション・ドメイン層から分離する。
 *
 * 【責務】
 * - ドメインエンティティを `AdministratorMapper` で `JpaEntity` に変換し、Spring Data リポジトリに委譲して保存・検索する。
 * - `EmailAddress` などの VO を JPA 変換可能な構造に対応させ、型安全なデータ処理を保証する。
 *
 * 【補足】
 * - `EmailAddress` は `@Converter` により DB カラムと相互変換可能な値オブジェクトである前提。
 * - インターフェースのみ定義すれば、Spring Boot 起動時に実装が自動生成される。
 *
 * 【注意】
 * - 本リポジトリはあくまで永続層（Infrastructure）であり、ドメイン層からは `AdministratorRepository` 経由で利用する。
 */
@Repository
class AdministratorJpaRepository(
    private val administratorSpringDataRepository: AdministratorSpringDataRepository,
    private val administratorMapper: AdministratorMapper
) : AdministratorRepository {

    override fun save(administrator: Administrator): Administrator {
        val entity = administratorMapper.toEntity(administrator)
        val savedEntity = administratorSpringDataRepository.save(entity)
        return administratorMapper.toDomain(savedEntity)
    }

    override fun findByEmail(email: EmailAddress): Administrator? {
        return administratorSpringDataRepository.findByEmail(email)
            ?.let { administratorMapper.toDomain(it) }
    }
}
