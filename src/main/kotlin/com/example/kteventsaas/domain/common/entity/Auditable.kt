package com.example.kteventsaas.domain.common.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.time.OffsetDateTime
import java.time.ZoneOffset

@MappedSuperclass
open class Auditable {

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    @PrePersist
    fun onPrePersist() {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        createdAt = now
        updatedAt = now
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
    }
}
