package com.example.kteventsaas.presentation.admin.tenant.dto

import com.example.kteventsaas.domain.tenant.entity.Tenant
import java.util.UUID

data class TenantResponse(
    val id: UUID,
    val name: String
) {
    companion object {
        fun from(tenant: Tenant): TenantResponse {
            return TenantResponse(
                id = tenant.id!!,
                name = tenant.name.value
            )
        }
    }
}
