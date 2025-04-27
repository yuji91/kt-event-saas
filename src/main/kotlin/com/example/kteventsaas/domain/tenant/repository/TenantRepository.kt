package com.example.kteventsaas.domain.tenant.repository

import com.example.kteventsaas.domain.tenant.entity.Tenant
import java.util.UUID

interface TenantRepository {

    fun save(tenant: Tenant): Tenant

    fun findById(id: UUID): Tenant?

    fun findByName(name: String): Tenant?

    fun findAll(): List<Tenant>
}
