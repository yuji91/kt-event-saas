package com.example.kteventsaas.domain.tenant.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "tenants")
open class Tenant(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    var name: String

)
