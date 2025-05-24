package com.example.kteventsaas.application.organizer.service.jwt

import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.stereotype.Component

/**
 * JWT トークン発行ユースケースを提供するコンポーネント
 *
 * 【役割】
 * - 認証成功後の Organizer ドメインモデルを元に JWT アクセストークンを発行する。
 *
 * 【責務】
 * - JwtPayloadFactory で作成されたクレーム情報を受け取り、
 *   JwtTokenProvider を用いて署名付きトークンを生成する。
 * - トークンの有効期限や署名アルゴリズムなど、環境設定をカプセル化する。
 *
 * 【補足】
 * - トークン検証やパースは JwtTokenProvider が担当し、
 *   発行処理に専念するシングルリスポンシビリティ設計となっている。
 *
 * 【注意】
 * - 現時点では、開発工数の都合上、アクセストークンとリフレッシュトークンは
 *   `tokenType` クレームによる区別のみに留めている。
 *   実運用を見据え、将来的には有効期限・署名キー・保存方針などを分離する拡張余地がある。
 */
@Component
class JwtIssuer(
    private val payloadFactory: JwtPayloadFactory,
    private val tokenProvider: JwtTokenProvider
) {

    /**
     * アクセストークンを発行
     */
    fun issue(organizer: Organizer): String {
        val claims = payloadFactory.createPayload(organizer)
            .toMutableMap().apply { put("tokenType", "access") }
        return tokenProvider.createToken(claims)
    }

    /**
     * リフレッシュトークンを発行（ここでは一旦、アクセストークンと同一ロジックを利用する）
     * （実運用ではリフレッシュ専用のクレームや有効期限を設定するケースが多い）
     */
    fun issueRefreshToken(organizer: Organizer): String {
        val claims = payloadFactory.createPayload(organizer)
            .toMutableMap().apply { put("tokenType", "refresh") }
        return tokenProvider.createToken(claims) // 必要に応じてリフレッシュ用のクレームや有効期限を設定
    }

    /**
     * アクセストークンの有効期間を秒単位で返す
     */
    fun expiresInSeconds(): Int {
        return (tokenProvider.getValidityInMs() / 1000).toInt()
    }
}
