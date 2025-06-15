# TenantAdminControllerIntegrationTest - 統合テスト観点一覧

このファイルでは、`TenantAdminControllerIntegrationTest` において実施されている統合テストの観点と、実装状況を整理します。  
バリデーション、永続化、レスポンスの整合性など、REST API の入出力が永続層と正しく連携できているかを重点的に検証します。

---

## ✅ テスト観点一覧

| 観点ID | テスト観点                                  | 検証内容                                                | この統合テストでの確認必要性 | 対応テストケース（メソッド名）                                              |
|--------|----------------------------------------------|----------------------------------------------------------|------------------------------|------------------------------------------------------------------------------|
| 1      | REST APIの正常系動作                         | `POST /admin/tenants` が 201 を返し、レスポンス構造が正しい         | ✅ 必須                       | `POST admin tenant creates new tenant and returns 201`                       |
| 2      | VO（Value Object）の @Converter 保存・取得確認   | `TenantName` が保存・復元で破綻しないこと                         | ✅ 必須                       | `POST then GET by ID returns same data with correct mapping`                |
| 3      | 一意制約（UNIQUE）違反時の例外ハンドリング       | テナント名の重複時に例外となり、HTTP 409 が返る                          | ✅ 必須                       | `POST duplicate tenant name returns 409 Conflict`                           |
| 4      | バリデーション異常時のHTTPレスポンス             | `@NotBlank` 等で入力不正を400として返す                              | ✅ 必須                       | `CreateTenantValidationTest` 内の各テストケース                            |
| 5      | Jacksonによる構造・型の整合性                     | 空フィールド、不正構造、型不一致時のエラーが期待通り返る                        | ✅ 必須                       | `nameが数値の場合`、`不正なJSON構造（括弧ミス）` など                      |
| 6      | トランザクション境界の確認                          | サービス層で例外発生時、永続化がロールバックされること                       | ✅ 必須                       | `POST that triggers service exception results in rollback`（※保留中）       |
| 7      | エラーレスポンス構造の検証                          | 異常時の JSON が契約通り（`errors[].field`, `message` 等）か             | ⭕ 推奨                       | `CreateTenantValidationTest` 内のエラー系                                 |
| 8      | Content-Type の確認                               | `Content-Type: application/json` が期待通り返る                          | ⭕ 推奨（環境依存）            | 一部 `content { contentType(...) }` により確認                             |
| 9      | POST → GET の一貫性                                | 登録直後に取得でき、同一内容であること                                     | ✅ 必須                       | `POST then GET by ID returns same data with correct mapping`                |
| 10     | 不要な観点：DTOの equals/hashCode、JSON構造の単体確認 | DTOの構造確認は**ユニットテストで済**                                   | ❌ 不要                       | `TenantResponseTest` でカバー済み                                          |
| 11     | 不要な観点：Service のモックによる分岐テスト         | ApplicationService の分岐はユニットテストでカバー                        | ❌ 不要                       | `TenantApplicationServiceTest` にて実施予定                                |

---

## ❗ 保留中の観点（@Disabled）

| 観点 | 概要                                                   | テストケース名                                                | 今後の対応方針                        |
|------|--------------------------------------------------------|---------------------------------------------------------------|---------------------------------------|
| 4    | `name = null` の場合のバリデーション失敗                  | `nameがnullの場合、400とバリデーションエラーが返る`            | テスト失敗理由を調査して復帰させる          |
| 6    | ApplicationService 例外時にロールバックされるかを検証     | `POST that triggers service exception results in rollback`    | 意図的に例外を発生させる仕組みを調整し検証   |

---

## 備考

- 本テストは Testcontainers + 実DB + Spring Boot 環境下での統合テストです。
- `ObjectMapper` によるシリアライズ確認は単体テスト（`TenantResponseTest`）で実施済みのため、本統合テストでは冗長に実施していません。

