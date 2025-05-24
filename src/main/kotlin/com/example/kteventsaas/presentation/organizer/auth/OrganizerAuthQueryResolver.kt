package com.example.kteventsaas.presentation.organizer.auth

import com.example.kteventsaas.application.organizer.service.OrganizerAuthService
import com.example.kteventsaas.presentation.organizer.auth.dto.OrganizerInfo
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

/**
 * Organizer 認証情報取得用 GraphQL Resolver
 *
 * 【役割】
 * - 認証済みの Organizer に関する情報を取得する GraphQL クエリ (`currentOrganizer`) を提供。
 *
 * 【責務】
 * - 現在のリクエストコンテキストに紐づく Organizer を `OrganizerAuthService` を通じて特定し、
 *   クライアントに返却する。
 *
 * 【補足】
 * - `@QueryMapping` により Spring for GraphQL が自動的に Resolver として認識。
 * - 認可処理は `SecurityFilterChain`（例：JWT 認証フィルタ）側で処理されるため、
 *   本 Resolver 内では認証済みユーザーであることを前提としている。
 *
 * 【注意】
 * - Spring for GraphQL では、Resolver クラスに `@Controller` を付与しないと
 *  `@QueryMapping` や `@MutationMapping` を認識しない。
 *
 * - `@Component` では Resolver として動作せず、GraphQL スキーマに登録されない。
 */
@Controller
class OrganizerAuthQueryResolver(
    private val authService: OrganizerAuthService
) {

    @QueryMapping
    fun currentOrganizer(): OrganizerInfo {
        val organizer = authService.resolveCurrentOrganizer()
        return OrganizerInfo(
            email = organizer.email.value,
            role = organizer.role.name,
            tenantId = organizer.tenantId
        )
    }
}
