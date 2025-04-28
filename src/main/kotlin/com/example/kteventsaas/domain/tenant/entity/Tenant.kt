package com.example.kteventsaas.domain.tenant.entity

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import java.util.UUID

data class Tenant(

    val id: UUID? = null,
    val name: TenantName
)
