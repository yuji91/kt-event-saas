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
 * カスタマー向け /customer 以下専用の Spring Security 設定クラス
 *
 * 【役割】
 * - Customer 用エンドポイントへのアクセスを JWT 認証で保護するセキュリティチェーンを定義。
 *
 * 【責務】
 * - `/customer/graphql` 配下のリクエストを JWT 認証で検証し、認可ルールを適用。
 * - カスタムフィルタ `JwtAuthenticationFilter` を適切な位置に組み込み、トークン検証を実施。
 * - CSRF を無効化し、ステートレス API として動作させる。
 *
 * 【補足】
 * - Organizer 用と分離して定義することで、責務の明確化とロール誤用を防止。
 */
@Configuration
@EnableWebSecurity
class CustomerSecurityConfig {

    @Bean
    fun customerJwtAuthenticationFilter(tokenProvider: JwtTokenProvider) = JwtAuthenticationFilter(tokenProvider)

    @Bean
    fun customerSecurityFilterChain(
        http: HttpSecurity,
        customerJwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .securityMatcher("/customer/graphql")
            .authorizeHttpRequests {
                it
                    .requestMatchers("/customer/graphql").permitAll()
                    .anyRequest().hasRole("CUSTOMER")
            }
            .addFilterBefore(customerJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .csrf { it.disable() }
            .build()
    }
}
