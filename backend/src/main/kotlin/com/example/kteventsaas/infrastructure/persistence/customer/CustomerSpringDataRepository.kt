package com.example.kteventsaas.infrastructure.persistence.customer

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import com.example.kteventsaas.infrastructure.persistence.customer.entity.CustomerJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

/**
 * Spring Data JPA によって自動実装される Customer 用のデータアクセスインターフェース
 *
 * ---
 * 【役割】
 * - `CustomerJpaEntity` を対象とした CRUD 操作や検索機能を提供する、Spring Data JPA リポジトリ。
 * - インフラ層において JPA を用いたデータアクセスのエントリポイントとして機能する。
 *
 * 【責務】
 * - `EmailAddress` による検索 (`findByEmail`) を定義し、認証処理に対応。
 * - ドメイン層インターフェース（`CustomerRepository`）の実装クラスから委譲されて使用される。
 *
 * 【補足】
 * - 本インターフェース自体には実装を記述せず、Spring Boot の起動時に動的に実装が生成される。
 * - `@Repository` アノテーションは不要（Spring Data により自動認識）。
 */
interface CustomerSpringDataRepository : JpaRepository<CustomerJpaEntity, UUID> {

    /**
     * 指定されたメールアドレスを持つ Customer を取得。存在しない場合は null を返す。
     */
    fun findByEmail(email: EmailAddress): CustomerJpaEntity?
}
