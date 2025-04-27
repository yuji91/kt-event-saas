package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TenantJpaRepository(
    private val tenantSpringDataRepository: TenantSpringDataRepository
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant {
        return tenantSpringDataRepository.save(tenant)
    }

    override fun findById(id: UUID): Tenant? {
        return tenantSpringDataRepository.findByIdOrNull(id)
    }

    override fun findByName(name: String): Tenant? {
        return tenantSpringDataRepository.findByName(name)
    }

    override fun findAll(): List<Tenant> {
        return tenantSpringDataRepository.findAll()
    }
}

// ここがSpring Data JPA用のインタフェース
interface TenantSpringDataRepository : JpaRepository<Tenant, UUID> {
    fun findByName(name: String): Tenant?
}
