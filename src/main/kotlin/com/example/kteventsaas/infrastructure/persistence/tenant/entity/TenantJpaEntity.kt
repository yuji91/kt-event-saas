package com.example.kteventsaas.infrastructure.persistence.tenant.entity

import com.example.kteventsaas.infrastructure.persistence.converter.TenantNameConverter
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.common.AuditableJpa
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "tenants")
class TenantJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    @Convert(converter = TenantNameConverter::class)
    var name: TenantName

) : AuditableJpa()
