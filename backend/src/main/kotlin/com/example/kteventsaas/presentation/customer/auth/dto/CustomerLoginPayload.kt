package com.example.kteventsaas.presentation.customer.auth.dto

/**
 * Customer ログイン／リフレッシュの返却DTO
 *
 * 【役割】
 * - GraphQL ミューテーションの返却値として、アクセストークンや認証情報を保持する。
 *
 * 【責務】
 * - アクセストークン、リフレッシュトークン（任意）、有効期限、権限情報を返す。
 *
 * 【GraphQLスキーマ対応】
 * type LoginPayload {
 *   accessToken: String!
 *   refreshToken: String
 *   expiresIn: Int!
 *   tenantId: ID!
 *   role: String!
 * }
 */
data class CustomerLoginPayload(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int,
    val tenantId: java.util.UUID,
    val role: String
)
