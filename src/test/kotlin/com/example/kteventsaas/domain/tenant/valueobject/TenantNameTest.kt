package com.example.kteventsaas.domain.tenant.valueobject

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Tag

/**
 * ===============================================================
 * TenantNameTest
 * ---------------------------------------------------------------
 * バリューオブジェクト TenantName の単体テスト
 *
 * ■ テスト設計方針
 * - 正常系 → 異常系 → 境界値系の順で整理し、網羅性を意識
 * - 例外発生時はエラーメッセージも厳密に検証
 * - ドメイン制約（空文字禁止、長さ制限）を重点的に確認
 *
 * ■ テスト対象機能
 * - コンストラクタ（バリデーション）
 * - toString()メソッド
 *
 * ■ 使用ライブラリ
 * - JUnit5
 * - AssertJ
 * ===============================================================
 */
@Tag("UnitTest")
class TenantNameTest {

    @Nested
    inner class TenantNameConstructor {

        // region Normal Cases - 正常系

        @Test
        fun `should create TenantName when value is valid`() {
            val tenantName = TenantName("ExampleTenant")
            assertThat(tenantName.value).isEqualTo("ExampleTenant")
        }

        // endregion

        // region Exceptional Cases - 異常系

        @Test
        fun `should throw exception when value is blank`() {
            assertThatThrownBy { TenantName("") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("must not be blank")
        }

        // endregion

        // region Boundary Cases - 境界値

        @Test
        fun `should throw exception when value exceeds 255 characters`() {
            val longName = "a".repeat(256)

            assertThatThrownBy { TenantName(longName) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("255 characters or less")
        }

        @Test
        fun `should create TenantName when value length is 255 characters`() {
            val validName = "a".repeat(255)
            val tenantName = TenantName(validName)
            assertThat(tenantName.value).isEqualTo(validName)
        }

        @Test
        fun `should create TenantName when value length is 254 characters`() {
            val validName = "a".repeat(254)
            val tenantName = TenantName(validName)
            assertThat(tenantName.value).isEqualTo(validName)
        }

        // endregion
    }

    @Nested
    inner class ToStringMethod {

        // region Normal Cases - 正常系

        @Test
        fun `should return value as string`() {
            val tenantName = TenantName("TenantString")
            assertThat(tenantName.toString()).isEqualTo("TenantString")
        }

        // endregion
    }
}
