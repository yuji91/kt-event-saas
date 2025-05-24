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
 * オーガナイザー向け /organizer 以下専用の Spring Security 設定クラス
 *
 * 【役割】
 * - Organizer 用エンドポイントへのアクセスを JWT 認証で保護するセキュリティチェーンを定義。
 *
 * 【責務】
 * - `/organizer/~` 配下のリクエストを JWT 認証で検証し、認可ルールを適用。
 * - カスタムフィルタ `JwtAuthenticationFilter` を適切な位置に組み込み、トークン検証を実施。
 * - CSRF を無効化し、ステートレス API として動作させる。
 *
 * 【補足】
 * - Administrator 用のセキュリティ設定とは別に定義し、役割と責務を明確化。
 * - `jwtAuthenticationFilter` はコンストラクタインジェクションで渡される想定。
 * - Bean メソッド注入で循環依存を回避
 *
 * 【切り替え理由】
 * - コンストラクタインジェクションでは `JwtAuthenticationFilter` と `OrganizerSecurityConfig` 間で循環依存が発生していたため、
 *   Bean メソッド注入に切り替え、プライマリコンストラクタを削除することで初期化順序を明確化
 *
 * 【注意】
 * - Spring for GraphQL の HTTP エンドポイントはデフォルトで /graphql
 *   GraphQL リクエストは /organizer/~ を通らないため、JWT 認証が 一切実行されていません
 */
@Configuration
@EnableWebSecurity // Spring Security 設定を有効化し、JWT 認証フィルタを Bean 登録する。
class OrganizerSecurityConfig {

    @Bean
    fun jwtAuthenticationFilter(tokenProvider: JwtTokenProvider) = JwtAuthenticationFilter(tokenProvider)

    @Bean
    fun organizerSecurityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter
    ): SecurityFilterChain {
        return http
            .securityMatcher("/organizer/graphql")
            .authorizeHttpRequests {
                it
                    // GraphQL エンドポイントには認証なしでもアクセス可（認証は GraphQL 側の Resolver で行う）
                    .requestMatchers("/organizer/graphql").permitAll()
                    // 他のリクエストは ORGANIZER 権限を要求（※今は /organizer/graphql のみ対象なので不要でもよい）
                    .anyRequest().hasRole("ORGANIZER")
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .csrf { it.disable() }
            .build()
    }
}
