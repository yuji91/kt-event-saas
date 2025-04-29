package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import com.example.kteventsaas.infrastructure.persistence.tenant.mapper.TenantMapper
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TenantJpaRepository(
    private val tenantSpringDataRepository: TenantSpringDataRepository,
    private val tenantMapper : TenantMapper
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant {
        val savedEntity = tenantSpringDataRepository.save(tenantMapper.toEntity(tenant))
        return tenantMapper.toDomain(savedEntity)
    }

    override fun findById(id: UUID): Tenant? {
        return tenantSpringDataRepository.findByIdOrNull(id)?.let { tenantMapper.toDomain(it) }
    }

    override fun findByName(name: TenantName): Tenant? {
        return tenantSpringDataRepository.findByName(name)?.let { tenantMapper.toDomain(it) }
    }

    override fun findAll(): List<Tenant> {
        return tenantSpringDataRepository.findAll().map { tenantMapper.toDomain(it) }
    }
}

// ここがSpring Data JPA用のインタフェース
interface TenantSpringDataRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByName(name: TenantName): TenantJpaEntity?
}
