package com.example.kteventsaas.presentation.admin.tenant

import com.example.kteventsaas.application.tenant.service.TenantApplicationService
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.presentation.admin.tenant.dto.CreateTenantRequest
import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import com.example.kteventsaas.presentation.common.exception.ErrorCodes
import com.example.kteventsaas.presentation.common.exception.ErrorResponse
import com.example.kteventsaas.presentation.common.exception.NotFoundException
import org.springframework.http.HttpStatus
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
            ?: throw NotFoundException("Tenant not found", errorCode = ErrorCodes.TENANT_NOT_FOUND)
        return TenantResponse.from(tenant)
    }

    @GetMapping("/name/{name}")
    fun getTenantByName(@PathVariable name: String): TenantResponse {
        val tenant = tenantApplicationService.getTenantByName(TenantName(name))
            ?: throw NotFoundException("Tenant not found", errorCode = ErrorCodes.TENANT_NOT_FOUND)
        return TenantResponse.from(tenant)
    }

    @GetMapping
    fun listTenants(): List<TenantResponse> {
        return tenantApplicationService.listTenants()
            .map { TenantResponse.from(it) }
    }

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        return ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            errorCode = e.errorCode,
            message = e.message
        )
    }
}
