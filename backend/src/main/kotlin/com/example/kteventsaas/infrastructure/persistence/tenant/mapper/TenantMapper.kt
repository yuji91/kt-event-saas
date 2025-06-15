package com.example.kteventsaas.infrastructure.persistence.tenant.mapper

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

/**
 * ドメイン層とインフラ層のエンティティを相互に変換するための MapStruct マッパー
 *
 * ---
 * 【役割】
 * - `Tenant`（ドメインエンティティ）と `TenantJpaEntity`（JPAエンティティ）の相互変換を行う。
 *
 * 【責務】
 * - ドメイン層が技術依存（JPAやSpring）を直接参照せずに済むよう、
 *   インフラ層で変換責務を引き受ける。
 * - DTO ↔ ドメインの変換と混在させず、**永続化専用の変換責務に限定する**。
 *
 * 【補足】
 * - `@Mapper(componentModel = "spring")` により、SpringのDI対象として扱われる。
 * - 実装は MapStruct によってコンパイル時に自動生成される。
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface TenantMapper {

    fun toEntity(domain: Tenant): TenantJpaEntity

    fun toDomain(entity: TenantJpaEntity): Tenant
}
