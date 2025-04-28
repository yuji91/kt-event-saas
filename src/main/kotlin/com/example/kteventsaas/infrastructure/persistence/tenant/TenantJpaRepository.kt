package com.example.kteventsaas.infrastructure.persistence.tenant

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.tenant.entity.TenantJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TenantJpaRepository(
    private val tenantSpringDataRepository: TenantSpringDataRepository
) : TenantRepository {

    override fun save(tenant: Tenant): Tenant {
        val savedEntity = tenantSpringDataRepository.save(tenant.toJpaEntity())
        return savedEntity.toDomain()
    }

    override fun findById(id: UUID): Tenant? {
        return tenantSpringDataRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun findByName(name: String): Tenant? {
        return tenantSpringDataRepository.findByName(name)?.toDomain()
    }

    override fun findAll(): List<Tenant> {
        return tenantSpringDataRepository.findAll().map { it.toDomain() }
    }

    private fun Tenant.toJpaEntity(): TenantJpaEntity = TenantJpaEntity(id = this.id, name = this.name.value)

    private fun TenantJpaEntity.toDomain(): Tenant {
        return Tenant(
            id = this.id,
            name = TenantName(this.name),
        )
    }
}

// ここがSpring Data JPA用のインタフェース
interface TenantSpringDataRepository : JpaRepository<TenantJpaEntity, UUID> {
    fun findByName(name: String): TenantJpaEntity?
}
