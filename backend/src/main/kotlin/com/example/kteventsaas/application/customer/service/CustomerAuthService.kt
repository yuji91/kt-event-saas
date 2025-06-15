package com.example.kteventsaas.application.customer.service

import com.example.kteventsaas.application.customer.service.jwt.JwtIssuer
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.domain.customer.entity.Customer
import com.example.kteventsaas.domain.customer.repository.CustomerRepository
import com.example.kteventsaas.infrastructure.security.jwt.JwtTokenProvider
import com.example.kteventsaas.presentation.common.exception.ErrorCodes
import com.example.kteventsaas.presentation.common.exception.NotFoundException
import com.example.kteventsaas.presentation.customer.auth.dto.CustomerLoginInput
import com.example.kteventsaas.presentation.customer.auth.dto.CustomerLoginPayload
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Customer 認証ユースケースを担うアプリケーションサービス
 *
 * 【役割】
 * - GraphQL Resolver からのログインおよびトークン再発行リクエストを受け、認証処理全体を統括する。
 *
 * 【責務】
 * - LoginInput で受け取った資格情報（メール・パスワード）を検証し、
 *   ドメインの Customer エンティティを取得・認証。
 * - 認証成功時に JwtIssuer を呼び出してアクセストークンを発行。
 * - リフレッシュトークンの検証および再発行のロジックを提供。
 */
@Service
class CustomerAuthService(
    private val repository: CustomerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtIssuer: JwtIssuer,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Transactional(readOnly = true)
    fun loginCustomer(input: CustomerLoginInput): CustomerLoginPayload {
        val customer = repository.findByEmail(EmailAddress(input.email))
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(input.password, customer.passwordDigest)) {
            throw BadCredentialsException("Invalid credentials")
        }

        val accessToken = jwtIssuer.issue(customer)
        val refreshToken = jwtIssuer.issueRefreshToken(customer)
        val expiresIn = jwtIssuer.expiresInSeconds()

        return CustomerLoginPayload(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = expiresIn,
            tenantId = customer.tenantId,
            role = customer.role.name
        )
    }

    @Transactional(readOnly = true)
    fun refreshToken(token: String): CustomerLoginPayload {
        if (!jwtTokenProvider.validateToken(token)) {
            throw IllegalArgumentException("リフレッシュトークンが無効です")
        }

        val claims: Claims = jwtTokenProvider.getClaims(token)
        if (claims["tokenType", String::class.java] != "refresh") {
            throw IllegalArgumentException("リフレッシュトークンではありません")
        }

        val customerId = claims.subject
        val customer = repository.findById(UUID.fromString(customerId))
            ?: throw NotFoundException("Customer が見つかりません", ErrorCodes.CUSTOMER_NOT_FOUND)

        val newAccessToken = jwtIssuer.issue(customer)
        val newRefreshToken = jwtIssuer.issueRefreshToken(customer)
        val expiresIn = jwtIssuer.expiresInSeconds()

        return CustomerLoginPayload(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = expiresIn,
            tenantId = customer.tenantId,
            role = customer.role.name
        )
    }

    @Transactional(readOnly = true)
    fun resolveCurrentCustomer(): Customer {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
            ?: throw AuthenticationCredentialsNotFoundException("認証情報がありません")

        if (!authentication.isAuthenticated ||
            authentication is AnonymousAuthenticationToken ||
            authentication.name == "anonymousUser"
        ) {
            throw AuthenticationCredentialsNotFoundException("認証情報がありません")
        }

        val customerId = runCatching { UUID.fromString(authentication.name) }
            .getOrElse {
                throw IllegalStateException("Invalid customer ID format in JWT sub")
            }

        return repository.findById(customerId)
            ?: throw NotFoundException("Customer not found: $customerId", ErrorCodes.CUSTOMER_NOT_FOUND)
    }
}