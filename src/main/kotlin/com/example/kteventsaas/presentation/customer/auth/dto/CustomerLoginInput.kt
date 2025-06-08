package com.example.kteventsaas.presentation.customer.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Customer ログインリクエスト DTO
 *
 * 【役割】
 * - GraphQL ミューテーションで受け取るログイン情報を保持する。
 *
 * 【責務】
 * - 入力バリデーション（空欄、形式、長さ）を付与し、GraphQL層での事前検証を可能にする。
 *
 * 【GraphQLスキーマ対応】
 * input LoginInput {
 *   email: String!
 *   password: String!
 * }
 */
data class CustomerLoginInput(

    @field:Email(message = "メールアドレスの形式が正しくありません")
    @field:NotBlank(message = "メールアドレスを入力してください")
    val email: String,

    @field:NotBlank(message = "パスワードを入力してください")
    @field:Size(min = 8, message = "パスワードは8文字以上で入力してください")
    val password: String
)
