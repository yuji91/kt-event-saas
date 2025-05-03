package com.example.kteventsaas.presentation.admin.tenant.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTenantRequest(
    @field:NotBlank(message = "Tenant name must not be blank")
    @field:Size(max = 255, message = "Tenant name must be 255 characters or less")
    val name: String
)
