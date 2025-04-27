package com.example.kteventsaas.domain.tenant.valueobject

@JvmInline
value class TenantName(val value: String) {
    init {
        require(value.isNotBlank()) { "Tenant name must not be blank" }
        require(value.length <= 255) { "Tenant name must be 255 characters or less" }
    }

    override fun toString(): String = value
}
