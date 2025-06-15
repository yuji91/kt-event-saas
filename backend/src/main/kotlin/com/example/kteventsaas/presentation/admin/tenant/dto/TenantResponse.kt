package com.example.kteventsaas.presentation.admin.tenant.dto

import com.example.kteventsaas.domain.tenant.entity.Tenant
import java.util.UUID

/**
 * テナント情報を返すためのレスポンスDTO
 *
 * ---
 * 【役割】
 * - REST API（管理者向け）からクライアントに返却するテナント情報を表現する。
 *
 * 【責務】
 * - ドメインエンティティ `Tenant` から必要な情報のみを抽出し、
 *   HTTPレスポンスとして安定的かつシリアライズ可能な構造に変換する。
 *
 * 【補足】
 * - ドメイン層のモデルをそのまま返すことを避けることで、API仕様とドメイン内部構造の分離を維持している。
 * - `from` メソッドにより、Controller層で明示的に変換を行う。
 */
data class TenantResponse(
    val id: UUID,
    val name: String
) {
    companion object {
        fun from(tenant: Tenant): TenantResponse {
            return TenantResponse(
                id = tenant.id!!,
                name = tenant.name.value
            )
        }
    }
}
