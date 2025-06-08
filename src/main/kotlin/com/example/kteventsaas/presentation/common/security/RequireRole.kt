package com.example.kteventsaas.presentation.common.security

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder

/**
 * 認証済ユーザーのロールを検証するユーティリティ関数群
 *
 * 【目的】
 * - GraphQL Resolver などで、現在のユーザーに期待するロールがあるかを検証する。
 *
 * 【背景】
 * - GraphQL はすべての操作が単一のエンドポイント（例：/graphql）に集約されるため、
 *   REST API のように URL 単位でのアクセス制御（hasRole, antMatchers）を行うことができない。
 * - そのため、個別の Query / Mutation（Resolver）単位での認可処理が必須となる。
 * - 本ユーティリティは、各 Resolver 内で明示的にロール検証を行うための共通関数を提供する。
 *
 * 【使用例】
 * - requireCurrentRole("CUSTOMER")
 *
 * 【補足】
 * - フィールドや状態を持たず、インスタンス生成やDIが不要な純粋なユーティリティ集のため class でなく object で定義
 */
object RequireRole {

    /**
     * 現在のユーザーのロールが指定されたものと一致するか確認する。
     * 一致しない場合は AccessDeniedException をスローする。
     *
     * @param expectedRole 期待するロール名（例："CUSTOMER", "ORGANIZER"）
     */
    fun requireCurrentRole(expectedRole: String) {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("Access denied: not authenticated")

        val actualRole = authentication.authorities.firstOrNull()?.authority
            ?: throw AccessDeniedException("Access denied: no role assigned")

        println("🔐 requireCurrentRole: expected=ROLE_$expectedRole, actual=$actualRole")

        if (actualRole != "ROLE_$expectedRole") {
            throw AccessDeniedException("Access denied: required=$expectedRole, actual=$actualRole")
        }
    }
}
