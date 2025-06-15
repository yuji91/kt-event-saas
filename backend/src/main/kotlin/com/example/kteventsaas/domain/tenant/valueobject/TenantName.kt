package com.example.kteventsaas.domain.tenant.valueobject

/**
 * テナント名を表すドメイン層の値オブジェクト（Value Object）
 *
 * ---
 * 【役割】
 * - 業務上の識別名であるテナント名を、安全かつ一貫した形で扱うためのラッパークラス。
 *
 * 【責務】
 * - 空文字や255文字超など、構文レベルでの妥当性検証を生成時に行う。
 * - 無効な値は生成段階で `IllegalArgumentException` を投げて拒否することで、
 *   ドメイン層以降では常に「正しいテナント名」が存在するという前提を保証する。
 *
 * 【補足】
 * - `toString()` をオーバーライドして、ログ出力や表示用途では `value` のみを返す。
 *   例：`ExampleTenant` → `"TenantName(value=ExampleTenant)"` ではなく `"ExampleTenant"` として出力
 *
 * 【注意】
 * - `@JvmInline value class` を用いた場合、一部のJacksonシリアライズ／デシリアライズ処理
 *   （例：POSTエラー後にGETで再取得時など）で意図しない挙動が発生するケースがあり、
 *   現在は `data class` として定義している（暫定対応）
 */
// TODO: @JvmInline value class で定義するとPOST重複エラー後のGETでエラーになる
data class TenantName(val value: String) {
    init {
        require(value.isNotBlank()) { "Tenant name must not be blank" }
        require(value.length <= 255) { "Tenant name must be 255 characters or less" }
    }

    // TenantName(value=ExampleTenant) のようなクラス名・プロパティ名を含む冗長な文字列ではなく、
    // value（テナント名）のみを返すように toString を再定義している
    override fun toString(): String = value
}
