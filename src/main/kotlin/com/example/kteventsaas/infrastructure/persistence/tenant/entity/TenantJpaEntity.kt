package com.example.kteventsaas.infrastructure.persistence.tenant.entity

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
    var name: String

) : AuditableJpa(){
    /** Hibernate 用 no-arg CTOR */
    protected constructor() : this(null, "")
}
