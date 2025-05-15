package com.example.kteventsaas.domain.common.valueobject

/**
 * メールアドレスを表すドメイン層の値オブジェクト（Value Object）
 *
 * ---
 * 【役割】
 * - メールアドレスとして入力された値を、安全かつ一貫した形で扱うためのラッパークラス。
 *
 * 【責務】
 * - 空文字、320文字超、メール形式違反などの構文チェックを生成時に行う。
 * - 無効な値は生成段階で `IllegalArgumentException` を投げて拒否し、
 *   ドメイン層以降では「正しいメールアドレス」のみが存在する前提を保証する。
 *
 * 【補足】
 * - `toString()` をオーバーライドし、ログ出力や表示用途では `value` のみを返す。
 *   例：`user@example.com` → `"EmailAddress(value=user@example.com)"` ではなく `"user@example.com"` として出力
 *
 * 【注意】
 * - `@JvmInline value class` を用いた場合、一部のJacksonシリアライズ／デシリアライズ処理
 *   （例：POSTエラー後にGETで再取得時など）で意図しない挙動が発生するケースがあり、
 *   現在は `data class` として定義している（暫定対応）
 */
// TODO: @JvmInline value class で定義するとPOST重複エラー後のGETでエラーになる?
//@JvmInline
data class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Email must not be blank" }
        require(value.length <= 320) { "Email must be 320 characters or less" }
        require(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(value)) {
            "Invalid email format: $value"
        }
    }

    override fun toString(): String = value
}
