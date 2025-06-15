package com.example.kteventsaas.infrastructure.persistence.organizer

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.organizer.entity.OrganizerJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA によって自動実装される Organizer 用のデータアクセスインターフェース
 *
 * ---
 * 【役割】
 * - `OrganizerJpaEntity` を対象とした CRUD 操作や検索機能を提供する、Spring Data JPA リポジトリ。
 * - インフラ層において JPA を用いたデータアクセスのエントリポイントとして機能する。
 *
 * 【責務】
 * - `EmailAddress` による検索 (`findByEmail`) を定義し、認証処理に対応。
 * - ドメイン層インターフェース（`OrganizerRepository`）の実装クラスから委譲されて使用される。
 *
 * 【補足】
 * - 本インターフェース自体には実装を記述せず、Spring Boot の起動時に動的に実装が生成される。
 * - `@Repository` アノテーションは通常不要（Spring Data が自動検出）。
 * - JPA Entity である `OrganizerJpaEntity` と一致する型のみを扱う。
 *
 * 【注意】
 * - `interface` である必要がある（Spring Data によるリフレクションで実装が生成されるため）。
 * - メソッド名は JPA の命名規約に従うこと（例：`findByEmail` で EmailAddress によるクエリを生成）。
 */
interface OrganizerSpringDataRepository : JpaRepository<OrganizerJpaEntity, UUID> {
    /**
     * 指定されたメールアドレスを持つ Organizer を取得。存在しない場合は null を返す。
     */
    fun findByEmail(email: EmailAddress): OrganizerJpaEntity?
}
