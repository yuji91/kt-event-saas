package com.example.kteventsaas.domain.tenant.entity

import com.example.kteventsaas.domain.common.entity.Auditable
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.domain.tenant.converter.TenantNameConverter
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "tenants")
open class Tenant(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    @Convert(converter = TenantNameConverter::class)
    var name: TenantName

) : Auditable(){
    // TODO: kotlin-jpa プラグイン導入でコンストラクタ定義が不要になるかもしれない
    protected constructor() : this(null, TenantName("default"))
}
