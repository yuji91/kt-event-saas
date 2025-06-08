package com.example.kteventsaas.presentation.customer.auth

import com.example.kteventsaas.application.customer.service.CustomerAuthService
import com.example.kteventsaas.presentation.common.security.RequireRole.requireCurrentRole
import com.example.kteventsaas.presentation.customer.auth.dto.CustomerInfo
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * Customer 認証情報取得用 GraphQL Resolver
 *
 * 【役割】
 * - 認証済みの Customer に関する情報を取得する GraphQL クエリ (`currentCustomer`) を提供。
 *
 * 【責務】
 * - 現在のリクエストコンテキストに紐づく Customer を `CustomerAuthService` を通じて特定し、
 *   クライアントに返却する。
 *
 * 【補足】
 * - `@QueryMapping` により Spring for GraphQL が自動的に Resolver として認識。
 * - 認可処理は SecurityFilterChain 側で行われるため、
 *   本 Resolver 内では認証済みユーザーであることを前提としている。
 */
@Controller
class CustomerAuthQueryResolver(
    private val authService: CustomerAuthService
) {

    @QueryMapping
    fun currentCustomer(): CustomerInfo {
        requireCurrentRole("PARTICIPANT")

        val customer = authService.resolveCurrentCustomer()
        return CustomerInfo(
            email = customer.email.value,
            role = customer.role.name,
            tenantId = customer.tenantId
        )
    }
}
