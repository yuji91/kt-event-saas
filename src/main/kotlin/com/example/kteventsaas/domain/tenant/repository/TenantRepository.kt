package com.example.kteventsaas.domain.tenant.repository

import com.example.kteventsaas.domain.tenant.entity.Tenant
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import java.util.UUID

interface TenantRepository {

    fun save(tenant: Tenant): Tenant

    fun findById(id: UUID): Tenant?

    fun findByName(name: TenantName): Tenant?

    fun findAll(): List<Tenant>
}
