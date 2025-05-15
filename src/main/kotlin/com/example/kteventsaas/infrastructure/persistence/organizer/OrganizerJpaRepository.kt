package com.example.kteventsaas.infrastructure.persistence.organizer

import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.domain.organizer.repository.OrganizerRepository
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.organizer.entity.OrganizerJpaEntity
import com.example.kteventsaas.infrastructure.persistence.organizer.mapper.OrganizerMapper
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * ドメイン層の `OrganizerRepository` を JPA + Spring Data を用いて実装するインフラ層の具象リポジトリ
 *
 * ---
 * 【役割】
 * - `OrganizerJpaEntity` による永続化操作をラップし、ドメイン層と DB アクセスの橋渡しを行う。
 * - 技術的関心（JPA, Spring Data）をアプリケーション・ドメイン層から分離する。
 *
 * 【責務】
 * - ドメインエンティティを `OrganizerMapper` で `JpaEntity` に変換し、Spring Data リポジトリに委譲して保存・検索する。
 * - `EmailAddress` などの VO を JPA 変換可能な構造に対応させ、型安全なデータ処理を保証する。
 *
 * 【補足】
 * - `EmailAddress` は `@Converter` により DB カラムと相互変換可能な値オブジェクトである前提。
 * - インターフェースのみ定義すれば、Spring Boot 起動時に実装が自動生成される。
 *
 * 【注意】
 * - 本リポジトリはあくまで永続層（Infrastructure）であり、ドメイン層からは `OrganizerRepository` 経由で利用する。
 */
@Repository
class OrganizerJpaRepository(
    private val springDataRepository: OrganizerSpringDataRepository,
    private val mapper: OrganizerMapper
) : OrganizerRepository {

    override fun save(organizer: Organizer): Organizer {
        val entity: OrganizerJpaEntity = mapper.toEntity(organizer)
        val saved: OrganizerJpaEntity = springDataRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun findByEmail(email: EmailAddress): Organizer? {
        return springDataRepository.findByEmail(email)
            ?.let(mapper::toDomain)
    }

    override fun findById(id: UUID): Organizer? {
        return springDataRepository.findByIdOrNull(id)
            ?.let(mapper::toDomain)
    }
}
