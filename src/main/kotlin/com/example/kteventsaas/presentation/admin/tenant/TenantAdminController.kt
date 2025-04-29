package com.example.kteventsaas.presentation.admin.tenant

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/admin/tenants")
class TenantAdminController(
    private val tenantApplicationService: TenantApplicationService
) {

    @PostMapping
    fun createTenant(@RequestBody request: CreateTenantRequest): TenantResponse {
        val tenant = tenantApplicationService.createTenant(request.name)
        return TenantResponse.from(tenant)
    }

    @GetMapping("/{id}")
    fun getTenant(@PathVariable id: UUID): TenantResponse {
        val tenant = tenantApplicationService.getTenant(id)
            ?: throw RuntimeException("Tenant not found")
        return TenantResponse.from(tenant)
    }

    @GetMapping("/name/{name}")
    fun getTenantByName(@PathVariable name: String): TenantResponse {
        val tenant = tenantApplicationService.getTenantByName(name)
            ?: throw RuntimeException("Tenant not found")
        return TenantResponse.from(tenant)
    }

    @GetMapping
    fun listTenants(): List<TenantResponse> {
        return tenantApplicationService.listTenants()
            .map { TenantResponse.from(it) }
    }
}
