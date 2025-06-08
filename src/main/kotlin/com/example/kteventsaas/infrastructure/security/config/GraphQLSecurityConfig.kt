package com.example.kteventsaas.infrastructure.security.config

import com.example.kteventsaas.infrastructure.security.jwt.JwtAuthenticationFilter
import com.example.kteventsaas.infrastructure.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Organizer / Customer 共通の GraphQL Security 設定
 *
 * 【役割】
 * - Spring for GraphQL の `/graphql` エンドポイントに対して、JWT 認証フィルターを適用する。
 *
 * 【責務】
 * - アクセストークンを Authorization: Bearer 形式で受け取り、JwtAuthenticationFilter で検証。
 * - Resolver 側で ロール(CUSTOMER/ORGANIZER)に応じた認可処理を実行する（SecurityChainではpermitAll）。
 *
 * 【補足】
 * - 認可は GraphQL Resolver に任せ、SecurityFilterChain では全許可。
 * - 管理者用セッション認証は別途 `/admin/-` で構成する。
*/
@Configuration
@EnableWebSecurity
class GraphQLSecurityConfig {

    /**
     * 共通の JWT 認証フィルタを定義
     */
    @Bean
    fun jwtAuthenticationFilter(tokenProvider: JwtTokenProvider): JwtAuthenticationFilter =
        JwtAuthenticationFilter(tokenProvider)

    /**
     * GraphQL 用のセキュリティチェーンを定義（/graphql エンドポイント専用）
     */
    @Bean
    fun graphqlSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .securityMatcher("/graphql") // Spring for GraphQL のエンドポイントに適用
            .authorizeHttpRequests {
                it
                    .requestMatchers("/graphql").permitAll() // Resolver で認証チェック
                    .anyRequest().denyAll()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .csrf { it.disable() }
            .build()
    }
}
