package com.example.kteventsaas.infrastructure.security.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

/**
 * JWT 発行および検証ユーティリティ
 *
 * 【役割】
 * - JWT トークンの生成と検証を行うインフラ層コンポーネント。
 *
 * 【責務】
 * - クレームマップを受け取り、署名付きトークンを作成。
 * - トークンの有効期限設定および署名アルゴリズムを管理。
 * - トークン文字列の検証（署名・有効期限チェック）機能を提供。
 * - トークンからクレーム情報を取得し、アプリケーション層に渡す。
 *
 * 【補足】
 * - アプリケーション層では JwtTokenProvider のメソッドを利用するのみで、
 *   トークン処理の詳細は本クラスが隠蔽される。
 *
 * 【注意】
 * - 現在は access／refresh の区別なくこのメソッドで発行している。
 * - 開発初期フェーズのため簡易的に一本化しているが、実運用では以下のような分離が推奨される：
 *   - アクセストークン用の `createAccessToken(claims)` メソッド
 *   - リフレッシュトークン用の `createRefreshToken(claims)` メソッド
 * - 有効期限、署名キー、使用方針（DB保存など）を明確に分離して保守性とセキュリティを高める予定。
 *
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expire-length}") private val validityInMs: Long
) {
    /**
     * トークン有効期間（ミリ秒）を返すアクセサ
     */
    fun getValidityInMs(): Long = validityInMs

    /**
     * 任意のクレーム（Map<String, Any>）を元に、有効期限つき・署名付きの JWT を生成して返す
     */
    // TODO: access／refresh トークンで生成メソッドを分割
    fun createToken(claims: Map<String, Any>): String {
        val now = Date()
        val expiry = Date(now.time + validityInMs)
        return Jwts.builder()
            .setClaims(claims) // ← Map 全体を JWT の ペイロード部（クレーム）としてセット
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS256, secretKey) // JWT に署名を付与
            .compact() // 上記のヘッダー・ペイロード・署名を Base64 エンコードして. でつなげて1つのトークン文字列にまとめる（JWTフォーマット）
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claimsJws = Jwts.parser()
                .setSigningKey(secretKey)     // 署名を検証するための秘密鍵をセット
                .parseClaimsJws(token)  // トークンの構文解析＋署名の検証（失敗すれば例外が投げられる）
            !claimsJws.body.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    /**
     *  JWTトークンのペイロード部分（例: sub, role, tenantId, tokenType など）を取り出すために使用
     */
    fun getClaims(token: String) =
        Jwts.parser()
            .setSigningKey(secretKey)     // 署名検証に使う秘密鍵を設定
            .parseClaimsJws(token)  // トークンをパース（署名検証＋構文解析）
            .body                        // クレーム部（payload）を取得
}
