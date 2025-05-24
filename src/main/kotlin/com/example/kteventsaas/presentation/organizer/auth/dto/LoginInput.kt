package com.example.kteventsaas.presentation.organizer.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Organizer ログインリクエスト DTO
 *
 * 【役割】
 * - GraphQL ミューテーションで受け取るログイン情報を保持する。
 *
 * 【責務】
 * - メールアドレスとパスワードの入力値に対し、プレゼンテーション層で構文バリデーションを実行する。
 * - 不正な入力（空文字・形式不正・文字数超過）を早期に弾き、アプリケーション層に安全な状態で渡す。
 *
 * 【補足】
 * - クラス名 `LoginInput` は GraphQL スキーマの入力型 `LoginInput` と一致させることで、スキーマとコードの対応を明確化している。
 * - この DTO は Controller（Resolver）でドメインの認証用オブジェクトへ変換する前提で設計。
 * - ビジネスルール（認可チェックやロックアウトなど）はアプリケーション/ドメイン層で担う。
 */
data class LoginInput(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 255, message = "Email must be 255 characters or less")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    @field:Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    val password: String
)
