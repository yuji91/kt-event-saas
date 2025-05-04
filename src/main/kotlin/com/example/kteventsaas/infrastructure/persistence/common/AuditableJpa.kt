package com.example.kteventsaas.infrastructure.persistence.common

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * インフラ層専用の監査ベースクラス
 *
 * ---
 * 【役割】
 * - Spring Data JPA の監査機能（Auditing）を活用して、
 *   永続化対象エンティティに以下カラムを自動付与する。
 *  - created_at  … INSERT 時に自動設定
 *  - updated_at  … INSERT／UPDATE 時に自動更新
 *  - version     … 楽観ロックで使用し、並列更新の整合性を保証（0で採番し、更新時にインクリメント）
 *
 * 【責務】
 * - 技術的関心（監査・楽観ロック）をドメイン層から分離し、JPAエンティティに限定して提供する。
 * - ドメインモデルには継承させず、インフラ層の `@Entity` クラス専用とする。
 *
 * 【運用要件】
 * - アプリケーション起動クラスまたは設定クラスに `@EnableJpaAuditing` が必要。
 * - Kotlin では Spring によるプロキシ生成のため、`open var` を使用する必要がある。
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
