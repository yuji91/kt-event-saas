package com.example.kteventsaas.infrastructure.persistence.customer.mapper

import com.example.kteventsaas.domain.customer.entity.Customer
import com.example.kteventsaas.infrastructure.persistence.customer.entity.CustomerJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

/**
 * ドメインモデル <-> JPAエンティティ の相互変換を担う MapStruct マッパー
 *
 * ---
 * 【役割】
 * - `Customer`（ドメイン層）と `CustomerJpaEntity`（永続化層）を相互に変換する。
 *
 * 【責務】
 * - 値オブジェクトや Enum などを含む構造の変換を担い、永続化層とドメイン層の依存を分離する。
 *
 * 【補足】
 * - `@Mapper(componentModel = "spring")` により、Spring の Bean として自動登録される。
 * - `EmailAddress` や `CustomerRole` は、JPA 側で `@Converter` または EnumType.STRING 対応済み。
 *
 * 【注意】
 * - unmappedTargetPolicy を IGNORE にすることで、
 *   明示的に必要な変換のみを対象とし、不足プロパティに警告を出さない。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface CustomerMapper {

    fun toDomain(entity: CustomerJpaEntity): Customer

    fun toEntity(domain: Customer): CustomerJpaEntity
}
