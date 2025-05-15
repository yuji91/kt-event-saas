package com.example.kteventsaas.domain.admin.entity

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID

/**
 * 管理者 を表すドメインエンティティ
 *
 * ---
 * 【役割】
 * - SaaSシステムを運営・管理する「管理者（Administrator）」という業務概念を表現する。
 *
 * 【責務】
 * - UUID による識別性を持ち、メールアドレス・パスワードハッシュ・ロール情報を保持する。
 * - 管理画面へのログインや、管理者権限に基づく認可判断などの基盤となる情報を提供する。
 *
 * 【補足】
 * - `id` は初期状態では null（未永続）とし、DB登録時に生成・付与される。
 * - `email` プロパティは `EmailAddress` を使用し、構文レベルの整合性が常に保証された状態となる。
 * - `Role` enum により誤った文字列値を防ぎ、権限・役割の情報は型安全に表現される。
 *
 * 【設計意図】
 * - このクラスはインフラ層の `AdministratorJpaEntity` とは明確に分離されており、
 *   JPAアノテーションやSpringの構成要素には依存しない。
 * - ドメイン層の純粋性を保ち、ユースケース／テストコードから容易に扱える軽量なエンティティとして定義している。
 * - data class を使用し、テスト・比較・MapStructとのマッピングを簡潔にしている。
 */
data class Administrator(
    val id: UUID? = null,
    val email: EmailAddress,
    val passwordDigest: String,
    val role: Role
) {
    enum class Role {
        SYS_ADMIN
    }
}
