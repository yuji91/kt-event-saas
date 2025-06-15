package com.example.kteventsaas.infrastructure.persistence.tenant.entity

import com.example.kteventsaas.infrastructure.persistence.converter.TenantNameConverter
import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import com.example.kteventsaas.infrastructure.persistence.common.AuditableJpa
import jakarta.persistence.*
import java.util.UUID

/**
 * tenants テーブルとマッピングされる JPA エンティティ
 *
 * ---
 * 【役割】
 * - テナント情報 をデータベースに永続化・取得するためのエンティティ。
 * - ドメインモデルとは異なるインフラ層専用のデータ表現。
 *
 * 【責務】
 * - DBとの入出力に特化し、ドメイン層が永続化技術に依存しないよう構成を分離する。
 * - UUID主キー、ユニーク制約、監査情報など、RDBスキーマ仕様に準拠した構造を保持。
 * - `TenantName` は JPA Converter（`TenantNameConverter`）を通じて、
 *   ValueObject のまま永続化・復元される。
 *
 * 【補足】
 * - 共通の監査項目（作成日時・更新日時など）は `AuditableJpa` を継承して提供。
 * - アプリケーション層／ドメイン層との相互変換は Mapper クラスが担う。
 * - このクラスは `Tenant` ドメインエンティティとは区別され、DBスキーマへの適合と技術的要件の反映を主目的とする。
 */
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
