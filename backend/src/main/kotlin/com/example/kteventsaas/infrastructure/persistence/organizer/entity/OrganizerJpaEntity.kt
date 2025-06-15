package com.example.kteventsaas.infrastructure.persistence.organizer.entity

import com.example.kteventsaas.domain.organizer.entity.Organizer.Role
import com.example.kteventsaas.infrastructure.persistence.common.AuditableJpa
import com.example.kteventsaas.infrastructure.persistence.converter.EmailAddressConverter
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import jakarta.persistence.*
import java.util.UUID

/**
 * organizers テーブルとマッピングされる JPA エンティティ
 *
 * ---
 * 【役割】
 * - Organizer の永続化・検索のためのインフラ層エンティティ。
 * - ドメイン層の純粋なモデル（Organizer）と切り分け、DB スキーマ依存を封じる。
 *
 * 【責務】
 * - UUID 主キー、外部キー (tenant_id)、ユニーク制約など DB 仕様を反映。
 * - メールアドレスは EmailAddressConverter 経由で Value Object ↔ String を相互変換。
 * - パスワードはハッシュ済みダイジェストとして保持。
 * - Role は列挙型（STRING）で格納。
 *
 * 【補足】
 * - 監査情報（作成・更新日時など）は AuditableJpa が提供。
 * - ドメイン層とのマッピングは OrganizerMapper が担う。
 *
 * 【注意】
 * - ドメインモデル (Organizer) とは別物。インフラ層でのみ使用する。
 */
@Entity
@Table(name = "organizers")
class OrganizerJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Column(name = "tenant_id", nullable = false)
    val tenantId: UUID,

    @Convert(converter = EmailAddressConverter::class)
    @Column(nullable = false)
    val email: EmailAddress,

    @Column(name = "password_digest", nullable = false)
    val passwordDigest: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.OWNER

) : AuditableJpa()
