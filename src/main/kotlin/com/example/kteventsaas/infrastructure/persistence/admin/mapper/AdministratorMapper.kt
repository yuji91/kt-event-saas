package com.example.kteventsaas.infrastructure.persistence.admin.mapper

import com.example.kteventsaas.domain.admin.entity.Administrator
import com.example.kteventsaas.infrastructure.persistence.admin.entity.AdministratorJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

/**
 * ドメインモデル <-> JPAエンティティ の相互変換を担う MapStruct マッパー
 *
 * ---
 * 【役割】
 * - `Administrator`（ドメイン層）と `AdministratorJpaEntity`（永続化層）を相互に変換する。
 *
 * 【責務】
 * - 値オブジェクトや Enum などを含む構造の変換を担い、永続化層とドメイン層の依存を分離する。
 *
 * 【補足】
 * - `@Mapper(componentModel = "spring")` により、Spring の Bean として自動登録される。
 * - `EmailAddress` と `Enum` は、すでに JPA 側で `@Converter` 対応済みである前提。
 *
 * 【注意】
 * - プロパティの変換が不足していても警告が出ないため、意図的に変換対象外とする項目にのみ使用すること。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE // build時の警告を抑制
)
interface AdministratorMapper {

    fun toDomain(entity: AdministratorJpaEntity): Administrator

    fun toEntity(domain: Administrator): AdministratorJpaEntity
}
