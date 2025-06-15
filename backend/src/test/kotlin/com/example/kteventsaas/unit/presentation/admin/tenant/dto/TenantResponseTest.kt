package com.example.kteventsaas.unit.presentation.admin.tenant.dto

import com.example.kteventsaas.presentation.admin.tenant.dto.TenantResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
/**
 * ===============================================================
 * TenantResponseTest
 * ---------------------------------------------------------------
 * DTOクラス TenantResponse の単体テスト
 *
 * ■ このテストの責務（WHAT）
 * - ドメインオブジェクトから変換された DTO が、期待通りの JSON にシリアライズされることを確認する
 * - JSON からのデシリアライズ結果が DTO として正しく復元されることを検証する
 * - DTO の等価性（equals/hashCode）および不変性を確認する
 * - JSON 構造の変更がリグレッション（後方互換性の破壊）を引き起こさないことを保証する
 *
 * ■ テスト手段（HOW）
 * - ObjectMapper によるシリアライズ／デシリアライズ処理を明示的に実行し、入出力の正当性を検証
 * - フィールド名・型・null制約など、Jackson 設定との整合性をチェック
 * - equals / hashCode の動作確認を通じて、DTO が「値オブジェクト」として正しく振る舞うかを確認
 * - 破壊的変更（フィールド名の変更・追加など）に対するリグレッションテストの役割も担う
 *
 * ■ テスト対象外の責務（NOT covered）
 * - サービス層・Controller 層でのバリデーションや例外処理
 * - 永続化（Entity とのマッピング）や DB アクセス
 * - API エンドポイントのステータスコードやルーティングの正当性（Controller テストで検証済）
 *
 * ■ テスト設計方針
 * - JSONシリアライズ／デシリアライズの正確性を担保する
 * - 非null制約やプロパティの型に関するJacksonの挙動を検証する
 * - DTOとしての等価性（equals / hashCode）や不変性を確認する
 *
 * ■ テスト対象機能
 * - Jacksonによるシリアライズ／デシリアライズ処理
 * - equals / hashCode メソッド
 *
 * ■ 使用ライブラリ
 * - AssertJ（org.assertj.core.api.Assertions）
 * - Jackson（com.fasterxml.jackson.databind.ObjectMapper）
 * - Spring Boot Test（@SpringBootTest により ObjectMapper を DI）
 *
 * ■ 主な検証観点
 * - シリアライズ後の JSON 構造（キー名、値の型、一貫性）
 * - デシリアライズ時の復元精度（フィールドの欠落・型不一致・未知キーの許容など）
 * - 空文字列や null 値の扱いに対する Jackson の挙動
 * - 値オブジェクトとしての等価性と hashCode の一貫性
 * ===============================================================
 */
@SpringBootTest
class TenantResponseTest {

    // SpringBoot は Jackson を内部で利用しており、ObjectMapper を DIコンテナに登録済
    // JSONのシリアライズとデシリアライズを行うためのクラス（Jacksonライブラリのコンポーネント）
    // @Autowired でテストクラスに自動注入する
    @Autowired
    lateinit var objectMapper: ObjectMapper

    // region - ドメイン→DTO の変換ロジック検証

    // テストは 「ドメインからDTOへの変換 → JSON表現」 の正確性を保証する
    // テスト観点：ドメインオブジェクトからDTO（TenantResponse）への変換が正しくシリアライズされること
    // テストの必要性
    // 1. 外部公開されるDTOが期待通りの値を返すことを確認するため
    // DTOはAPIレスポンスなどでクライアントに返す形式のため、期待通りの構造・フィールドが含まれていることを確認する
    // 2. Jacksonシリアライズ設定の検証
    // objectMapper.writeValueAsString(dto) を使うことで、Jacksonの設定や @JsonProperty 等のアノテーションが意図通り動作しているか確認

    // ドメイン層 -> DTO層 -> JSONシリアライズによるデータ移行の整合性を確認
    @Test
    fun `from should map id and name correctly`() {

        // --- Arrange ---
        val id = UUID.randomUUID()
        val dto = TenantResponse(id, "bar")

        // --- Act ---
        val json = objectMapper.writeValueAsString(dto)

        // --- Assert ---
        assertThat(json).contains("\"id\":\"$id\"")
        assertThat(json).contains("\"name\":\"bar\"")

    }

    // TODO: jackson-module-kotlin を使用する場合は jacksonObjectMapper で書き換える
    // シリアライズ・デシリアライズが正しく行われること（オブジェクトを JSON 文字列に変換した後、再びオブジェクトに戻しても、同一のデータが保持されているか）
    @Test
    fun `serialization and deserialization preserve object equality`() {

        // 1. Jackson が TenantResponse を正しく扱える構造になっているか
        //   - プロパティに対して適切にシリアライズ／デシリアライズできること
        // 2. @JsonCreator, @JsonProperty 等のアノテーションが必要ない構造になっているか
        //   - Kotlinでは引数付きコンストラクタや val プロパティでハマりがちな箇所がチェックできる
        // 3. JSON 経由でやりとりするAPI（RESTやKafkaなど）で破綻しないか
        //   - 実際のアプリで使われる JSON I/O の整合性を保証するリグレッションテストにもなる

        // --- Arrange ---
        val original = TenantResponse(UUID.randomUUID(), "example")

        // --- Act ---
        // Serialize : Kotlinオブジェクトを JSON 文字列に変換
        val json = objectMapper.writeValueAsString(original)

        // Deserialize : JSON 文字列を再び Kotlinオブジェクトへ変換
        val deserialized: TenantResponse = objectMapper.readValue(json, TenantResponse::class.java)

        // --- Assert ---
        assertThat(deserialized.id).isEqualTo(original.id)
        assertThat(deserialized.name).isEqualTo(original.name)
    }

    // TODO: jackson-module-kotlin を使用する場合は jacksonObjectMapper で書き換える
    // region - シリアライズ／デシリアライズの検証
    @Test
    fun `serialization should includes id and name fields correctly`() {

        // --- Arrange ---
        val id = UUID.randomUUID()
        val dto = TenantResponse(id, "bar")

        // --- Act ---
        val json = objectMapper.writeValueAsString(dto)

        // --- Assert ---
        assertThat(json).contains("\"id\":\"$id\"")
        assertThat(json).contains("\"name\":\"bar\"")
    }

    // 1. JSON構造の明示的な検証（シリアライズ内容）
    // 2. DTOの値による等価性の確認（ラウンドトリップ）
    @Test
    fun `TenantResponse should be serialized and deserialized correctly with all fields`() {

        // --- Arrange ---
        val id = UUID.randomUUID()
        val name = "example"
        val originalDto = TenantResponse(id, name)

        // --- Act ---
        // Serialize : Kotlinオブジェクトを JSON 文字列に変換
        val json = objectMapper.writeValueAsString(originalDto)
        // Deserialize : JSON 文字列を再び Kotlinオブジェクトへ変換
        val deserializedDto = objectMapper.readValue(json, TenantResponse::class.java)

        // --- Assert ---
        // シリアライズ内容（JSON文字列）を確認：明示的なリグレッションチェック
        assertThat(json).contains("\"id\":\"$id\"")
        assertThat(json).contains("\"name\":\"$name\"")

        // デシリアライズ結果の内容と等価性を確認（ラウンドトリップ）
        assertThat(deserializedDto).isEqualTo(originalDto) // data class であれば全フィールド検証可
    }

    // 前提: jackson-module-kotlin を 使っていない
    // jackson-module-kotlin がないと Jackson は Kotlin の null 安全性（val name: String と val name: String? の違い）を 理解しない。
    // Jackson がコンストラクタを使ってインスタンス化を試みると、コンストラクタ引数に null を渡してしまい失敗する
    // これは型不一致ではなく、インスタンス生成時の失敗（ValueInstantiationException）となる
    @Test
    fun `deserialization should fail when name is null`() {
        val json = """{"id":"${UUID.randomUUID()}","name":null}"""
        assertThatThrownBy {
            objectMapper.readValue(json, TenantResponse::class.java)
        }.isInstanceOf(ValueInstantiationException::class.java)
//        }.isInstanceOf(com.fasterxml.jackson.databind.exc.MismatchedInputException::class.java) // 型のミスマッチではなく、非 nullable パラメータに null を渡したことで「インスタンス生成時に失敗」するエラーとなる
    }

    // Jacksonによるデシリアライズ処理が「空文字列」を正しく処理できるかを検証
    // フィールドに空文字が渡されても、DTOが正しく生成されること（nullとみなされず空文字として保持されること）を保証
    @Test
    fun `deserialization should succeed when name is empty string`() {

        // --- Arrange: nameフィールドに空文字を指定
        val id = UUID.randomUUID()
        val json = """{"id":"$id","name":""}"""

        // --- Act: JSON文字列から TenantResponse オブジェクトを生成（デシリアライズ）
        val dto = objectMapper.readValue(json, TenantResponse::class.java)

        // --- Assert: 空文字として保持されること
        assertThat(dto.name).isEqualTo("")
    }

    // JacksonがJSONに含まれる未知のフィールド（この場合 "extra"）を無視し、正常にデシリアライズできることを確認する
    // ※ Jackson の設定で DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false になっていないと失敗するので注意
    @Test
    fun `deserialization should ignore unknown properties`() {

        // --- Arrange: JSONに予期しないフィールド（未知のキー）が混入
        val id = UUID.randomUUID()
        val json = """{"id":"$id","name":"foo","extra":"value"}"""

        // --- Act: JSON文字列から TenantResponse オブジェクトを生成（デシリアライズ）
        val dto = objectMapper.readValue(json, TenantResponse::class.java)

        // --- Assert: DTOへのデシリアライズ処理が壊れずに行われることを保証
        assertThat(dto.id).isEqualTo(id)
        assertThat(dto.name).isEqualTo("foo")
    }

    // endregion

    // region - 等価性・不変性の確認（同じ値を持つ別インスタンスが「等価」と判定されるか）

    // 「DTOが同じ値を持つ場合、equals や hashCode が期待通りに動作するか？」
    // DTOは「入力・出力を保持するだけ」の責務であり、**同一内容なら同一と見なせるべき（参照より値が重要）
    // 値による等価判定が成り立つことを保証するのは、DTOの本質的な性質の確認になる
    // テスト対象のDTOが data class かつ override がないため、Kotlinのメソッドをテストする必要はないが、仕様変更の検知用として残しておく
    // equals と hashCode の一貫性を確認（２つのオブジェクトが等価な場合の振る舞い）
    @Test
    fun `equals and hashCode behave as expected`() {

        // --- Arrange ---
        val id = UUID.randomUUID()

        // --- Act ---
        val a = TenantResponse(id, "x")
        val b = TenantResponse(id, "x")

        // --- Assert ---
        assertThat(a).isEqualTo(b) // テスト対象が data class でない場合は override しないと false になる
        assertThat(a.hashCode()).isEqualTo(b.hashCode()) // hashCode の比較も同一であること
    }

    // endregion
}
