package com.example.kteventsaas.domain.tenant.entity

import com.example.kteventsaas.domain.tenant.valueobject.TenantName
import java.util.UUID

/**
 * テナントを表すドメインエンティティ
 *
 * ---
 * 【役割】
 * - システム上で一意に識別される「テナント」という業務概念を表現する。
 *
 * 【責務】
 * - ID（UUID）による識別性を持ち、テナント名を値オブジェクトとして保持する。
 * - 業務ロジックの中心として、ドメイン層内で一貫したテナント情報の取り扱いを担う。
 *
 * 【補足】
 * - IDは初期状態では null（未永続）であり、DB登録時に生成・付与される。
 * - name プロパティは `TenantName` を使用し、構文レベルの整合性が常に保証された状態となる。
 *
 * 【設計意図】
 * - このエンティティはインフラ層の `TenantJpaEntity` とは明確に分離されており、
 *   JPAアノテーションやSpring固有の構成には一切依存していない。
 * - これにより、ドメイン層は技術的関心事から独立し、テスト容易性や移植性が確保されている。
 */
data class Tenant(

    val id: UUID? = null,
    val name: TenantName
)
