package com.example.kteventsaas.application.customer.service.jwt

import com.example.kteventsaas.domain.customer.entity.Customer
import org.springframework.stereotype.Component

/**
 * JWT ペイロード生成ユーティリティ（Customer用）
 *
 * 【役割】
 * - 認証対象の Customer ドメインモデルから JWT クレームマップを組み立てる。
 *
 * 【責務】
 * - Customer の属性（ID、ロール、テナントID）を抽出し、
 *   JWT 仕様に則ったペイロード構造を提供する。
 *
 * 【補足】
 * - Issuer や署名ロジックは JwtIssuer/JwtTokenProvider に委譲し、
 *   本コンポーネントはあくまでクレームデータの整形に専念する。
 * - テナントや認可情報が増えた場合にも拡張しやすい設計とする。
 */
@Component("customerJwtPayloadFactory")
class JwtPayloadFactory {

    fun createPayload(customer: Customer): Map<String, Any> {
        return mapOf(
            "sub" to customer.id.toString(),
            "role" to customer.role.name,
            "tenantId" to customer.tenantId.toString()
        )
    }
}
