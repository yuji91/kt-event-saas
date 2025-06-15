package com.example.kteventsaas.infrastructure.persistence.admin.entity

import com.example.kteventsaas.infrastructure.persistence.converter.EmailAddressConverter
import com.example.kteventsaas.domain.admin.entity.Administrator.Role
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.common.AuditableJpa
import jakarta.persistence.*
import java.util.*

/**
 * administrators テーブルとマッピングされる JPA エンティティ
 *
 * ---
 * 【役割】
 * - 管理者情報 をデータベースに永続化・取得するためのエンティティ。
 * - ドメインモデルとは異なるインフラ層専用のデータ表現。
 *
 * 【責務】
 * - DBとの入出力に特化し、ドメイン層が永続化技術に依存しないよう構成を分離する。
 * - UUID主キー、ユニーク制約、監査情報など、RDBスキーマ仕様に準拠した構造を保持。
 * - EmailAddress 型のフィールドは、JPA Converter（EmailAddressConverter）を介して
 *   String 型に変換され、ドメインの ValueObject として保持される。
 *
 * 【補足】
 * - 共通の監査項目（作成日時・更新日時など）は `AuditableJpa` を継承して提供。
 * - アプリケーション層／ドメイン層との相互変換は Mapper クラスが担う。
 * - このクラスは `Administrator` ドメインエンティティとは区別され、DBスキーマへの適合と技術的要件の反映を主目的とする。
 *
 * 【注意】
 * - DomainEntity に合わせて、VO型で定義すること
 *
 */
@Entity
@Table(name = "administrators")
class AdministratorJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Convert(converter = EmailAddressConverter::class)
    @Column(nullable = false, unique = true)
    val email: EmailAddress,

    @Column(name = "password_digest", nullable = false)
    val passwordDigest: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.SYS_ADMIN

) : AuditableJpa()
