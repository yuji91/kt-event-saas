package com.example.kteventsaas.presentation.organizer.auth

import com.example.kteventsaas.presentation.organizer.auth.dto.LoginInput
import com.example.kteventsaas.presentation.organizer.auth.dto.LoginPayload
import com.example.kteventsaas.application.organizer.service.OrganizerAuthService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

/**
 * Organizer ログイン用 GraphQL Resolver
 *
 * 【役割】
 * - GraphQL の `loginOrganizer` と `refreshOrganizerToken` ミューテーションを受け付け、
 *   認証サービスへリクエストを委譲する。
 *
 * 【責務】
 * - クライアントから渡された `LoginInput` とトークン文字列を `OrganizerAuthService` へ渡し、
 *   認証結果として `LoginPayload` を返却。
 * - サービス層でスローされた例外はそのまま伝播し、
 *   Spring for GraphQL のエラーハンドリング機構へ委ねる。
 *
 * 【補足】
 * - `@Controller` および `@MutationMapping` アノテーションにより、
 *   Spring for GraphQL が自動的にハンドラとして認識。
 * - 利用者の認可チェックは `SecurityFilterChain` 側で行われるため、
 *   本 Resolver 内での権限検証は行わない。
 */
@Controller
class OrganizerAuthMutationResolver(
    private val authService: OrganizerAuthService
) {

    @MutationMapping
    fun loginOrganizer(@Argument input: LoginInput): LoginPayload {
        return authService.loginOrganizer(input)
    }

    @MutationMapping
    fun refreshOrganizerToken(@Argument token: String): LoginPayload {
        return authService.refreshToken(token)
    }
}
