package com.example.kteventsaas.application.organizer.service

import com.example.kteventsaas.application.organizer.service.jwt.JwtIssuer
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.domain.organizer.repository.OrganizerRepository
import com.example.kteventsaas.infrastructure.security.jwt.JwtTokenProvider
import com.example.kteventsaas.presentation.common.exception.ErrorCodes
import com.example.kteventsaas.presentation.common.exception.NotFoundException
import com.example.kteventsaas.presentation.organizer.auth.dto.OrganizerLoginInput
import com.example.kteventsaas.presentation.organizer.auth.dto.OrganizerLoginPayload
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

/**
 * Organizer 認証ユースケースを担うアプリケーションサービス
 *
 * 【役割】
 * - GraphQL Resolver からのログインおよびトークン再発行リクエストを受け、認証処理全体を統括する。
 *
 * 【責務】
 * - LoginInput で受け取った資格情報（メール・パスワード）を検証し、
 *   ドメインの Organizer エンティティを取得・認証。
 * - 認証成功時に JwtIssuer を呼び出してアクセストークンを発行。
 * - リフレッシュトークンの検証および再発行のロジックを提供。
 *
 * 【補足】
 * - 認証以外のビジネスロジックは含めず、ドメインリポジトリへの参照取得と JWT 発行に専念する。
 * - トークンペイロードの組み立ては JwtPayloadFactory に委譲し、
 *   ドメイン固有の構造を意識せずに利用できるようにしている。
 */
@Service
class OrganizerAuthService(
    private val repository: OrganizerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtIssuer: JwtIssuer,                // アクセストークン／リフレッシュトークンの発行ロジック
    private val jwtTokenProvider: JwtTokenProvider   // JWT の署名検証・パースを行うコンポーネント
    // TODO: interface TokenProvider を定義して注入させると、テスト時にモック差し替えができる利点がある
) {
    @Transactional(readOnly = true)
    fun loginOrganizer(input: OrganizerLoginInput): OrganizerLoginPayload {
        // 1. メールで Organizer を取得
        val organizer = repository.findByEmail(EmailAddress(input.email))
            ?: throw BadCredentialsException("Invalid credentials")

        // 2. パスワード検証 (例: BCrypt)
        if (!passwordEncoder.matches(input.password, organizer.passwordDigest)) {
            throw BadCredentialsException("Invalid credentials")
        }

        // 3. JWT 発行
        val accessToken  = jwtIssuer.issue(organizer)
        val refreshToken = jwtIssuer.issueRefreshToken(organizer) // あれば実装
        val expiresIn    = jwtIssuer.expiresInSeconds()

        // 4. レスポンス組み立て
        return OrganizerLoginPayload(
            accessToken  = accessToken,
            refreshToken = refreshToken,
            expiresIn    = expiresIn,
            tenantId     = organizer.tenantId,
            role         = organizer.role.name
        )
    }

    @Transactional(readOnly = true)
    fun refreshToken(token: String): OrganizerLoginPayload {
        // 1. 署名検証＆期限チェック
        if (!jwtTokenProvider.validateToken(token)) {
            throw IllegalArgumentException("リフレッシュトークンが無効です") // TODO: InvalidTokenException を定義
        }

        // 2. クレーム取得
        val claims: Claims = jwtTokenProvider.getClaims(token)

        // 3. トークン種別チェック（accessToken と混同しないように）
        if (claims["tokenType", String::class.java] != "refresh") {
            throw IllegalArgumentException("リフレッシュトークンではありません") // TODO: InvalidTokenException を定義
        }

        // 4. クレームから organizerId を取得して存在確認
        val organizerId = claims.subject // クレーム sub には UUID（organizer.id）を格納している想定
        val organizer = repository.findById(UUID.fromString(organizerId))
            ?: throw NotFoundException("Organizer が見つかりません", errorCode = ErrorCodes.ORGANIZER_NOT_FOUND)

        // 5. 新しいアクセストークン＆リフレッシュトークンを発行
        val newAccessToken  = jwtIssuer.issue(organizer) // issueAccessToken の方が明示的？
        val newRefreshToken = jwtIssuer.issueRefreshToken(organizer)
        val expiresIn       = jwtIssuer.expiresInSeconds()

        // 5. GraphQL の返却型（LoginPayload）に詰めて返却
        return OrganizerLoginPayload(
            accessToken  = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn    = expiresIn,
            tenantId     = organizer.tenantId,
            role         = organizer.role.name
        )
    }

    /**
     * 現在認証済みの Organizer を取得する
     *
     * 【想定】
     * - JWT の sub に Organizer の UUID が入っている前提
     * - `JwtAuthenticationFilter` により認証が済んでおり、`SecurityContextHolder` から取り出せる
     */
    @Transactional(readOnly = true)
    fun resolveCurrentOrganizer(): Organizer {
        // 認証情報自体がないケースを弾く
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationCredentialsNotFoundException("認証情報がありません")

        // 匿名認証 (anonymousUser) も弾く
        if (!authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name == "anonymousUser"
        ) {
            throw AuthenticationCredentialsNotFoundException("認証情報がありません")
        }
        println("🔐 Authentication: $authentication")

        // sub (authentication.name) の形式チェック
        val organizerId = try {
            UUID.fromString(authentication.name)
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("Invalid organizer ID format in JWT sub") // 形式不正も同じ 401 として扱う
        }
        println("🔍 Organizer ID: $organizerId")

        // 実在確認
        return repository.findById(organizerId)
            ?: throw NotFoundException( "Organizer not found: $organizerId", ErrorCodes.ORGANIZER_NOT_FOUND)
    }
}
