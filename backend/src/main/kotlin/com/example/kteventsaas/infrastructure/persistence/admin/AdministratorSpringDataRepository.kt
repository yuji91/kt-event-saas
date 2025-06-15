package com.example.kteventsaas.infrastructure.persistence.admin

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.admin.entity.AdministratorJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA によって自動実装される管理者用のデータアクセスインターフェース
 *
 * ---
 * 【役割】
 * - `AdministratorJpaEntity` を対象とした CRUD 操作や検索機能を提供する、Spring Data JPA リポジトリ。
 * - インフラ層において JPA を用いたデータアクセスのエントリポイントとして機能する。
 *
 * 【責務】
 * - `EmailAddress` による検索 (`findByEmail`) を定義し、ログイン処理に対応。
 * - ドメイン層インターフェース（`AdministratorRepository`）の実装クラスから委譲されて使用される。
 *
 * 【補足】
 * - 本インターフェース自体には実装を記述せず、Spring Boot の起動時に動的に実装が生成される。
 * - `@Repository` アノテーションは通常必要ない（上位で定義されたクラスに付与）。
 * - JPA Entity である `AdministratorJpaEntity` と一致する型のみを扱う。
 *
 * 【注意】
 * - `interface` である必要がある（Spring Data によりリフレクションで実装されるため）
 * - メソッド名は JPA の命名規約に従うこと（例：`findByEmail` → EmailAddress によるクエリを生成）
 */
interface AdministratorSpringDataRepository : JpaRepository<AdministratorJpaEntity, UUID>  {
  fun findByEmail(email: EmailAddress): AdministratorJpaEntity?
}
