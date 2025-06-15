package com.example.kteventsaas.presentation.customer.auth.dto

import java.util.UUID

/**
 * 認証済み Customer 情報の返却DTO
 *
 * 【役割】
 * - `currentCustomer` クエリで返される Customer の情報を保持する。
 *
 * 【責務】
 * - 認証済みユーザーの最小限の識別情報（メール・ロール・テナント）を提供。
 *
 * 【GraphQLスキーマ対応】
 * type CustomerInfo {
 *   email: String!
 *   role: String!
 *   tenantId: ID!
 * }
 */
data class CustomerInfo(
    val email: String,
    val role: String,
    val tenantId: UUID
)
