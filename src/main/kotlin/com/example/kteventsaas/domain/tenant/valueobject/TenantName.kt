package com.example.kteventsaas.domain.tenant.valueobject

// TODO: @JvmInline value class で定義するとPOST重複エラー後のGETでエラーになる
data class TenantName(val value: String) {
    init {
        require(value.isNotBlank()) { "Tenant name must not be blank" }
        require(value.length <= 255) { "Tenant name must be 255 characters or less" }
    }

    // TenantName(value=ExampleTenant) のようなクラス名・プロパティ名を含む冗長な文字列ではなく、
    // value（テナント名）のみを返すように toString を再定義している
    override fun toString(): String = value
}
