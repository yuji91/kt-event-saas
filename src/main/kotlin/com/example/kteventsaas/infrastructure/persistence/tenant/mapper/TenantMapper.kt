package com.example.kteventsaas.infrastructure.persistence.tenant.mapper

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface TenantMapper {

    fun toEntity(domain: Tenant): TenantJpaEntity

    fun toDomain(entity: TenantJpaEntity): Tenant
}
