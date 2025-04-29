package com.example.kteventsaas.application.tenant.service

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.repository.TenantRepository
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TenantApplicationService(
    private val tenantRepository: TenantRepository
) {

    fun createTenant(name: String): Tenant {
        val tenant = Tenant(
            name = TenantName(name)
        )
        return tenantRepository.save(tenant)
    }

    fun getTenant(id: UUID): Tenant? {
        return tenantRepository.findById(id)
    }

    fun getTenantByName(name: String): Tenant? {
        return tenantRepository.findByName(TenantName(name))
    }

    fun listTenants(): List<Tenant> {
        return tenantRepository.findAll()
    }
}
