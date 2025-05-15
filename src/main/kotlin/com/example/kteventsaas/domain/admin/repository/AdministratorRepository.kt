package com.example.kteventsaas.domain.admin.repository

import com.example.kteventsaas.domain.admin.entity.Administrator
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID

/**
 * 管理者の永続化操作を定義するドメイン層のリポジトリインターフェース
 *
 * ---
 * 【役割】
 * - アプリケーション層や認証処理から管理者情報の保存・取得を行うための抽象的な契約。
 *
 * 【責務】
 * - 管理者エンティティに対する保存、IDやメールアドレスによる検索機能を定義する。
 * - 実際の永続化方式（JPA, JDBC, 外部サービスなど）に依存しない。
 *
 * 【補足】
 * - Spring Security の `UserDetailsService` 実装クラス（`AdministratorAuthService`）から呼ばれる主要なアクセスポイント。
 * - `EmailAddress` はドメイン層のValue Objectであり、単なる `String` に依存しない設計としている。
 *
 * 【注意】
 * - このインターフェースはドメイン層に配置されており、実装（`AdministratorJpaRepository`）はインフラ層で定義する。
 * - テストや開発時にモック実装に置き換えることで、永続化に依存しないユースケース検証が可能。
 */
interface AdministratorRepository {

    fun save(administrator: Administrator): Administrator

    fun findByEmail(email: EmailAddress): Administrator?
}
