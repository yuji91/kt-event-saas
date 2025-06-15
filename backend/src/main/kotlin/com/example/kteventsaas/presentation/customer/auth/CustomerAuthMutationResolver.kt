package com.example.kteventsaas.presentation.customer.auth

import com.example.kteventsaas.application.customer.service.CustomerAuthService
import com.example.kteventsaas.presentation.customer.auth.dto.CustomerLoginInput
import com.example.kteventsaas.presentation.customer.auth.dto.CustomerLoginPayload
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

/**
 * Customer ログイン用 GraphQL Resolver
 *
 * 【役割】
 * - GraphQL の `loginCustomer` と `refreshCustomerToken` ミューテーションを受け付け、
 *   認証サービスへリクエストを委譲する。
 *
 * 【責務】
 * - クライアントから渡された `LoginInput` とトークン文字列を `CustomerAuthService` へ渡し、
 *   認証結果として `LoginPayload` を返却。
 * - サービス層でスローされた例外はそのまま伝播し、
 *   Spring for GraphQL のエラーハンドリング機構へ委ねる。
 */
@Controller
class CustomerAuthMutationResolver(
    private val authService: CustomerAuthService
) {

    @MutationMapping
    fun loginCustomer(@Argument input: CustomerLoginInput): CustomerLoginPayload {
        return authService.loginCustomer(input)
    }

    @MutationMapping
    fun refreshCustomerToken(@Argument token: String): CustomerLoginPayload {
        return authService.refreshToken(token)
    }
}
