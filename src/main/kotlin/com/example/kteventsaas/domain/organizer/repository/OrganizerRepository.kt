package com.example.kteventsaas.domain.organizer.repository

import com.example.kteventsaas.domain.organizer.entity.Organizer
import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import java.util.UUID
/**
 * Organizer の永続化操作を定義するドメイン層のリポジトリインターフェース
 *
 * ---
 * 【役割】
 * - アプリケーション層や認証処理から Organizer 情報の保存・取得を行うための抽象的契約。
 *
 * 【責務】
 * - Organizer エンティティに対する保存、ID やメールアドレスによる検索機能を定義。
 * - 永続化方式（JPA, JDBC, 外部サービス等）に依存しない。
 *
 * 【補足】
 * - 認証サービス（OrganizerAuthService）から呼ばれる主要アクセスポイント。
 * - `EmailAddress` はドメイン層の Value Object であり、生の文字列に依存しない設計。
 *
 * 【注意】
 * - インターフェースはドメイン層にのみ配置し、実装はインフラ層で定義。
 * - テスト時にはモック実装に置き換えることで、永続化の有無を問わずユースケース検証可能。
 */
interface OrganizerRepository {

    fun save(organizer: Organizer): Organizer

    fun findByEmail(email: EmailAddress): Organizer?

    fun findById(id: UUID): Organizer?
}
