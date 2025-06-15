package com.example.kteventsaas.infrastructure.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 認証フィルタ
 *
 * 【役割】
 * - HTTP リクエストの Authorization ヘッダから JWT を取得し、認証情報を検証・設定する。
 *
 * 【責務】
 * - トークン文字列を `JwtTokenProvider` で検証し、有効期限・署名をチェック。
 * - クレーム情報（`sub`, `role`, `tenantId`）を抽出し、
 *   Spring Security の `Authentication` オブジェクトを生成。
 * - `SecurityContextHolder` に認証情報をセットし、以降のフィルター／コントローラ処理に引き継ぐ。
 *
 * 【補足】
 * - 無効／欠落トークンの場合は認証設定を行わずに次のフィルタへ委譲し、
 *   SecurityConfig によるアクセス制御で拒否される。
 * - トークンから生成する `Authentication` の権限はクレームのロール名に `ROLE_` プレフィックスを付与して設定。
 */
@Component
class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            println("🛡️ JWT Token received: $token")

            if (tokenProvider.validateToken(token)) {
                println("🛡️ token validated OK")
                val claims = tokenProvider.getClaims(token)
                val username = claims.subject
                val role = claims["role"] as String
                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val auth: Authentication = UsernamePasswordAuthenticationToken(username, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
    }
}
