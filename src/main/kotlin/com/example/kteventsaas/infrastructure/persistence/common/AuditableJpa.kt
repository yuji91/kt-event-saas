package com.example.kteventsaas.infrastructure.persistence.common

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * インフラ層専用の監査ベースクラス。
 * Spring Data JPA Auditing を利用して
 *  - created_at  … INSERT 時に自動設定
 *  - updated_at  … INSERT／UPDATE 時に自動更新
 *  - version     … 楽観ロック
 *
 * （アプリ起動クラスに @EnableJpaAuditing が必要）
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AuditableJpa {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: LocalDateTime? = null

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    open var updatedAt: LocalDateTime? = null

    @Version
    @Column(name = "version", nullable = false)
    open var version: Long = 0
}
