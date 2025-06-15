package com.example.kteventsaas.application.admin.service

import com.example.kteventsaas.domain.admin.repository.AdministratorRepository
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * 管理者認証ユースケースを担うアプリケーションサービス
 *
 * ---
 * 【役割】
 * - Spring Security の認証処理に連携するための `UserDetailsService` 実装クラス。
 * - 入力されたメールアドレスに対応する管理者アカウントを取得し、認証可能なユーザー情報として返却する。
 *
 * 【責務】
 * - メールアドレス（文字列）を `EmailAddress` Value Object に変換、構文チェックを通過させる。
 * - `AdministratorRepository` を介してドメイン層から管理者情報を取得し、該当がなければ例外を投げる。
 * - ドメインの認証情報を Spring Security が扱える `UserDetails` に変換して返却する。
 *
 * 【補足】
 * - 認証失敗時には `UsernameNotFoundException` を投げることで、Spring Security の認証エラーハンドリングが機能する。
 * - このサービスはフォーム認証（セッションベース）専用であり、REST API や JWT等の認可機構とは分離して設計されている。
 *
 * 【注意】
 * - Spring Security の仕様上、ユーザー名（username）として `String` 型のメールアドレスを使用している。
 * - 認証対象が増える場合（Organizer/Customer等）、ドメインごとに `UserDetailsService` を切り分けることでセキュリティ設定を分離できる。
 */
@Service
class AdministratorAuthService(
    private val administratorRepository: AdministratorRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val email = try {
            EmailAddress(username)
        } catch (e: IllegalArgumentException) {
            throw UsernameNotFoundException("Invalid email format") // 内部向け、画面には表示されない
        }

        val admin = administratorRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("Administrator not found") // 内部向け、画面には表示されない

        return User.builder()
            .username(admin.email.value)
            .password(admin.passwordDigest)
            .roles(admin.role.name)
            .build()
    }
}
