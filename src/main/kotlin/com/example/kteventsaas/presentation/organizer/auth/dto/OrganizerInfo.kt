package com.example.kteventsaas.presentation.organizer.auth.dto

import java.util.UUID

/**
 * Organizer 認証済みユーザー情報 DTO
 *
 * 【役割】
 * - 認証済みの Organizer に関する情報（メールアドレス・ロール・テナントID）をクライアントへ返却するための出力型。
 *
 * 【責務】
 * - GraphQL の `currentOrganizer` クエリに対するレスポンスボディとして利用され、
 *   フロントエンドがログインユーザーの属性を把握するための情報を提供する。
 *
 * 【補足】
 * - GraphQL スキーマの出力型 `OrganizerInfo` に対応する。
 * - 認証後のユーザー情報表示やロールによる画面制御に利用される前提で設計されている。
 * - セキュリティ上の観点から、パスワードなどの機密情報は含まれない。
 */
data class OrganizerInfo(
    val email: String,
    val role: String,
    val tenantId: UUID
)
