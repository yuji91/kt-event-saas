package com.example.kteventsaas.presentation.organizer.auth.dto

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID

/**
 * Organizer ログインレスポンス DTO
 *
 * 【役割】
 * - 認証成功時にクライアントへ返却する JWT および関連情報を保持する。
 *
 * 【責務】
 * - JWT トークン（アクセストークン・リフレッシュトークン）および有効期限、
 *   テナント ID、ロール情報をシリアライズ可能な形で提供する。
 * - クライアントが認証状態を維持・管理するための必要情報を網羅的に含む。
 *
 * 【補足】
 * - クラス名 `LoginPayload` は GraphQL スキーマの出力型 `LoginPayload` と一致させ、
 *   スキーマと実装の対応を明確化している。
 * - パスワードや内部秘密情報は含めず、トークンと最小限の認証情報のみを保持。
 */
data class OrganizerLoginPayload(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int,
    val tenantId: UUID,
    val email: EmailAddress,
    val role: String
)
