package com.example.kteventsaas.infrastructure.persistence.organizer.mapper

import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.infrastructure.persistence.organizer.entity.OrganizerJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

/**
 * ドメインモデル <-> JPAエンティティ の相互変換を担う MapStruct マッパー
 *
 * ---
 * 【役割】
 * - `Organizer`（ドメイン層）と `OrganizerJpaEntity`（永続化層）を相互に変換する。
 *
 * 【責務】
 * - 値オブジェクトや Enum などを含む構造の変換を担い、永続化層とドメイン層の依存を分離する。
 *
 * 【補足】
 * - `@Mapper(componentModel = "spring")` により、Spring の Bean として自動登録される。
 * - `EmailAddress` や `OrganizerRole` は、JPA 側で `@Converter` または EnumType STRING 対応済み。
 *
 * 【注意】
 * - プロパティの変換が不足していても警告が出ないため、意図的に変換対象外とする項目にのみ使用すること。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface OrganizerMapper {

    fun toDomain(entity: OrganizerJpaEntity): Organizer

    fun toEntity(domain: Organizer): OrganizerJpaEntity
}
