package com.example.kteventsaas.domain.customer.entity

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID

/**
 * Customer を表すドメインエンティティ
 *
 * 【役割】
 * - イベントに参加するエンドユーザー（Customer）を表す。
 *
 * 【責務】
 * - UUIDによる識別、メール、パスワードハッシュ、ロール、テナント所属を持つ。
 *
 * 【補足】
 * - ロールは現時点では `PARTICIPANT` のみだが、将来の拡張に備えて Enum化。
 */
data class Customer(
    val id: UUID? = null,
    val tenantId: UUID,
    val email: EmailAddress,
    val passwordDigest: String,
    val role: Role = Role.PARTICIPANT,
    val isActive: Boolean = true
) {
    enum class Role {
        PARTICIPANT
    }
}
