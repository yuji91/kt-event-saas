package com.example.kteventsaas.application.customer.service.jwt

import com.example.kteventsaas.domain.customer.entity.Customer
import com.example.kteventsaas.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.stereotype.Component

/**
 * JWT トークン発行ユースケースを提供するコンポーネント（Customer用）
 *
 * 【役割】
 * - 認証成功後の Customer ドメインモデルを元に JWT アクセストークンを発行する。
 *
 * 【責務】
 * - JwtPayloadFactory で作成されたクレーム情報を受け取り、
 *   JwtTokenProvider を用いて署名付きトークンを生成する。
 * - トークンの有効期限や署名アルゴリズムなど、環境設定をカプセル化する。
 *
 * 【補足】
 * - トークン検証やパースは JwtTokenProvider が担当し、
 *   発行処理に専念するシングルリスポンシビリティ設計となっている。
 */
@Component("customerJwtIssuer")
class JwtIssuer(
    private val payloadFactory: JwtPayloadFactory,
    private val tokenProvider: JwtTokenProvider
) {

    fun issue(customer: Customer): String {
        val claims = payloadFactory.createPayload(customer)
            .toMutableMap().apply { put("tokenType", "access") }
        return tokenProvider.createToken(claims)
    }

    fun issueRefreshToken(customer: Customer): String {
        val claims = payloadFactory.createPayload(customer)
            .toMutableMap().apply { put("tokenType", "refresh") }
        return tokenProvider.createToken(claims)
    }

    fun expiresInSeconds(): Int {
        return (tokenProvider.getValidityInMs() / 1000).toInt()
    }
}
