package com.example.kteventsaas.infrastructure.security

import com.example.kteventsaas.application.admin.service.AdministratorAuthService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

/**
 * 管理者向け /admin 以下専用の Spring Security 設定クラス
 *
 * ---
 * 【役割】
 * - 管理画面向けのセキュリティ制約（セッション認証）を定義。
 * - `/admin/~`をセッション認証で保護し、フォームログインを提供する
 * - `AdministratorAuthService` を使って認証を行い、ログイン・ログアウトの遷移や保護対象URLの指定を行う。
 *
 * 【責務】
 * - ログイン、ログアウト画面のURL、成功・失敗時の挙動、CSRFの有効化などを制御。
 * - フォーム認証に必要な `AuthenticationProvider` の定義と連携。
 * - CSRF有効化、PasswordEncoder（BCrypt）の定義。
 * - `DaoAuthenticationProvider` の認証サービスとして `AdministratorAuthService` を認証プロバイダとして登録
 *
 * 【補足】
 * - 管理者専用のチェーンのため、Organizer／Customer とは分離したセキュリティ定義を持つ。
 * - `securityMatcher("/admin/~")` により、本設定は `/admin` 配下に限定される。
 *
 * 【注意】
 * - Spring Security の `UserDetailsService` 実装が `AdministratorAuthService` に固定されているため、
 *   ドメインごとのセキュリティチェーン分離が前提となる。
 */
@Configuration
@EnableWebSecurity
class AdminSecurityConfig(
    private val administratorAuthService: AdministratorAuthService
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(administratorAuthService)
        provider.setPasswordEncoder(passwordEncoder())
        return provider
    }

    @Bean
    fun authenticationManager(
        config: AuthenticationConfiguration
    ): AuthenticationManager = config.authenticationManager

    @Bean
    fun adminSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .securityMatcher("/admin/**") // ① /admin/** だけをこのチェーンで扱う
            .authorizeHttpRequests { auth ->           // ② 認可ルール
                auth
                    // --- 許可したいものを先に書く ---
                    .requestMatchers(
                        "/admin/login", // GET
                        "/admin/login/**",         // ?error, ?logout など全部
                        "/admin/login/auth",       // POST
                        "/admin/css/**",
                        "/admin/js/**"
                    ).permitAll()
                    // --- それ以外は認証必須 ---
                    .anyRequest().authenticated()
            }
            .formLogin { login ->                    // ③ フォーム認証
                login
                    .loginPage("/admin/login")
                    .loginProcessingUrl("/admin/login/auth")
                    .defaultSuccessUrl("/admin/dashboard", true)
                    .failureUrl("/admin/login?error=true")
            }
            .logout { logout ->
                logout
                    .logoutUrl("/admin/logout")
                    .logoutSuccessUrl("/admin/login?logout=true")
            }
            .csrf(withDefaults())

        return http.build()
    }
}