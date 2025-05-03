# テスト戦略・方針ガイドライン

このプロジェクトにおけるテスト設計と実装方針を示します。

---

## ✅ テストレイヤーごとの責務

| レイヤー                            | テスト種別    | 責務                                         |
| ------------------------------- | -------- | ------------------------------------------ |
| ドメイン層 (`entity`, `valueobject`) | ユニットテスト  | ビジネスルール、整合性チェック、値オブジェクトの不変性など              |
| アプリケーション層 (`service`)           | ユニットテスト  | UseCaseの振る舞い、入力値に応じたRepository呼び出し、結果の制御など |
| リポジトリ層 (`repository`)           | 統合テスト    | DBへの永続化・検索の動作確認（Mockを使わない）                 |
| DTO層 (`dto`)                    | ユニットテスト  | JSON I/Oの正確性、等価性・不変性の検証                    |
| Web層（`controller`）              | 単体・結合テスト | HTTP入出力・バリデーション・ステータス・例外制御などの振る舞い検証        |

---

## 🧪 ドメイン層テストの設計指針

### 責務（テストすべきこと）

* エンティティ（Entity）の振る舞い（状態遷移、ドメインメソッド）の検証
* 値オブジェクト（ValueObject）の不変性と等価性の保証
* ファクトリ、集約ルート、ドメインサービスのロジック検証（DDD的観点）
* バリデーションや制約（例：名前の最大長など）の単体確認

### テスト対象外（この層で検証しないこと）

* データ永続化やリポジトリの存在（Repository 層で検証）
* アプリケーションサービスとの連携（Service 層で検証）
* REST コントローラを通じた I/O（Controller 層で検証）

### 実装方針

* `@Test` + `assertThat` によるメソッドの振る舞い・状態変化の明示的検証
* イミュータブルであることの検証（値オブジェクト）
* 値の生成時に発生する例外やエラーの明確な検証（バリデーション）

---

## 🧪 アプリケーションサービス層テストの設計指針

### 責務（テストすべきこと）

* アプリケーションサービスの振る舞いが仕様通りであること
* 正常系・異常系（null返却・空リスト）を網羅すること
* ドメイン層との境界が正しく保たれていること

### テスト対象外（この層で検証しないこと）

* `Tenant` などのエンティティ内部のロジック（→ドメイン層でユニットテスト）
* `TenantRepository` の具体的なDB実装（→統合テストで検証）
* REST層（Controller）との接続やAPI仕様（→結合テスト）
* トランザクションや例外処理の伝播（→設計上の仕様による）

---

## 🧪 DTO層（TenantResponseなど）のテスト設計指針

### 責務（テストすべきこと）

* DTO（`data class`）の **等価性・不変性**（`equals` / `hashCode`）の検証
* **Jackson による JSON I/O の整合性**（シリアライズ／デシリアライズの正確性）
* **@JsonProperty などのアノテーション**が意図通りに機能しているかの確認
* 外部 API（REST, Kafka など）との **契約（データ構造）のリグレッション検出**
* 空文字列や `null`、未知のフィールドを含む JSON に対する **復元動作の検証**

### テスト対象外（この層で検証しないこと）

* DBマッピングや `Entity` との変換ロジック（それは Repository または Mapper 層の責務）
* REST Controller でのステータスコードやバリデーション挙動の確認
* Service や Controller のビジネスロジック
* DTO を返す API の出力としての「表示確認」（Controller 層で担保）

### 実装方針

* `ObjectMapper.writeValueAsString()` / `readValue()` を使って JSON のラウンドトリップを明示的に確認
* 意図しないシリアライズ漏れや構造の変更に対して、**JSON 内容を文字列でアサート**
* 空文字や `null` を含む入力、および未知のプロパティに対して **Jackson の許容動作を明示的にテスト**
* data class の特性を利用し、`equals` や `hashCode` の一貫性も担保する
* JSON I/O に関する破壊的変更を検知するためのリグレッションテストとしても機能させる

---

## 🧪 コントローラ層（TenantAdminControllerなど）のテスト設計指針

### 責務（テストすべきこと）

* HTTPリクエストとレスポンスのステータス・構造の検証
* 不正な入力に対するバリデーションと適切なステータス（400/409など）返却の検証
* サービス層が正しく呼ばれるか（引数、呼び出し回数など）
* 例外（`MethodArgumentNotValidException`、`ResponseStatusException`など）の発生と内容

### テスト対象外（この層で検証しないこと）

* サービス層内部のロジック（重複チェックや永続化）
* グローバル例外ハンドラのレスポンスフォーマット（別途統合テストで検証）
* DTOやエンティティの変換ロジック（`TenantResponse.from()`など）
* Spring Bootの構成や依存解決の検証（@SpringBootTestの起動確認は統合テストで）

### テスト実装の基本方針

* `MockK`を使用し、**Stub**（返却値固定）と**Mock**（呼び出し検証）を明確に使い分ける
* バリデーション失敗時にサービスが**呼ばれていない**ことも検証
* ステータスコードのアサートは `mockMvc` と `jsonPath` を活用
* `@SpringBootTest + @AutoConfigureMockMvc` を使って依存解決の課題を回避

---

## 🧪 Testcontainer を用いた統合テスト設計方針

### 目的

* 実際の PostgreSQL（などのDB）コンテナを起動し、Spring Boot アプリケーションと統合的に動作検証する
* JPA設定、マイグレーション（Flywayなど）、リポジトリ・サービス・コントローラの接続性を含めたシナリオを確認する

### 対象範囲

* `@SpringBootTest` + Testcontainers により **本番に近い構成でのE2E的統合テスト** を実施
* 複数エンティティの連携・永続化・検索ロジックの実体検証
* `@Transactional` の境界とロールバック、例外伝播の挙動

### 実装方針

* `@Testcontainers` + `@Container` アノテーションでPostgreSQL起動
* プロファイル `test` を明示し、application-test.yml 等で接続先上書き
* JPAクエリやRepositoryの `@Query` の実行確認も対象に含める
* データの初期化には Flyway または SQL スクリプトを使用

---

## 🛠 使用ライブラリ

* **MockK**（RepositoryやServiceのモック）
* **JUnit5**（テストフレームワーク）
* **AssertJ**（アサーション）
* **Jackson**（DTOのシリアライズ／デシリアライズ検証）
* **Spring MockMvc**（Controller層の振る舞い検証）
* **Testcontainers**（DBを含む統合環境の動的起動）

---

## 🧾 命名規則・構成方針

* `XxxServiceTest` は `XxxService` に対するユニットテスト
* `XxxResponseTest` は DTOのシリアライズや等価性テスト
* `XxxControllerTest` は APIの入出力・ステータス検証を中心としたテスト
* `XxxIntegrationTest` は Testcontainer を使った統合テスト
* `@Nested` クラス名はメソッドや機能単位で明示的に
* コメントで `// region 正常系` `// region 異常系` などの分類を徹底

---

## 📁 ディレクトリ構成（例）

```
src/test/kotlin/com/example/kteventsaas/
├── application/
│   └── tenant/
│       └── service/
│           └── TenantApplicationServiceTest.kt
├── domain/
│   └── tenant/
│       └── valueobject/
│           └── TenantNameTest.kt
├── presentation/
│   └── admin/
│       └── tenant/
│           ├── dto/
│           │   └── TenantResponseTest.kt
│           └── TenantAdminControllerTest.kt
├── testconfig/
│   └── MockTenantApplicationServiceConfig.kt
├── KtEventSaasApplicationTests.kt
├── TestKtEventSaasApplication.kt
└── README.md ← このファイル
```

---

## 📌 備考

* 共通のMockパターンやObjectMapper設定は `test-utils` などで一元化予定
* JSON構造の変更検知やAPI契約の保持にはスナップショットテストの導入も検討
* 今後、Testcontainerを用いたControllerの統合テストも追加予定
