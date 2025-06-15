package com.example.kteventsaas.domain.organizer.entity

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID

/**
 * Organizer を表すドメインエンティティ
 *
 * ---
 * 【役割】
 * - テナントに紐付く「主催者（Organizer）」という業務概念を表現する。
 *
 * 【責務】
 * - UUID による識別性を持ち、メールアドレス・パスワードハッシュ・ロールを保持する。
 * - ログイン認証や認可判断の基盤となる情報を提供する。
 *
 * 【補足】
 * - `id` は永続化前は null とし、DB登録時に生成される。
 * - `email` は `EmailAddress` を使用し、構文レベルの整合性を保証する。
 * - `Role` enum により誤った文字列値を防ぎ、権限・役割の情報は型安全に表現される。
 *
 * 【設計意図】
 * - インフラ層の `OrganizerJpaEntity` と分離し、JPA 依存を持たないドメイン純粋なモデルとする。
 * - `data class` として定義し、テストやマッピングを容易に行えるよう設計。
 */
data class Organizer(
    val id: UUID? = null,
    val tenantId: UUID,
    val email: EmailAddress,
    val passwordDigest: String,
    val role: Role
) {
    enum class Role {
        OWNER
    }
}
