package com.example.kteventsaas.infrastructure.persistence.converter

import com.example.kteventsaas.domain.common.valueobject.EmailAddress
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * ドメインの値オブジェクト `EmailAddress` と DB の文字列型（PostgreSQLでは `VARCHAR` 相当）を相互に変換する JPA 属性コンバータ
 *
 * ---
 * 【役割】
 * - ドメインの `EmailAddress` を永続化可能な `String` 型に変換してDBに保存・復元する。
 *
 * 【責務】
 * - JPAエンティティ内で `@Convert` を付与されたフィールドに対し、自動的に変換ロジックを適用する。
 * - ドメイン層が JPA や DB型への依存を持たないよう、変換処理の責務をインフラ層に集約する。
 *
 * 【設計意図】
 * - ドメイン層の純粋性（技術非依存）を守るため、永続化時の型変換をインフラ層に閉じ込める。
 * - `autoApply = false` に設定することで、変換は明示的な `@Convert` のみで適用される（グローバル適用を避ける）。
 */
@Converter(autoApply = false)
class EmailAddressConverter : AttributeConverter<EmailAddress, String> {
    override fun convertToDatabaseColumn(attribute: EmailAddress?): String? =
        attribute?.value

    override fun convertToEntityAttribute(dbData: String?): EmailAddress? =
        dbData?.let { EmailAddress(it) }
}
