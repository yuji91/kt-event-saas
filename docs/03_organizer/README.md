# Organizer 認証機構 設計

本ドキュメントは、ChatGPTで行った Organizer 認証機構 のドキュメントです。  
設計方針（ADR要点）、DDDレイヤ構成、各クラスの役割表、ファイルツリーを記載します。

---

## ✅ 設計方針（ADR要約）

### 決定事項

* Organizer はテナント単位で管理されるユーザーとして認識される
* 認証方式に JWT を採用し、ステートレスAPIとして認証を実現
* Organizer は OWNER ロールとして Tenant 登録時に必ず1名定義される
* JWTの証明情報に `tenantId`, `role`, `sub` (メール) を含める
* REST でなく GraphQL スキーマファーストを採用し、スキーマと1:1対応させる

### 理由

* SaaS構成下でのマルチテナント認証に対応するため
* JWTを用いることで、SPAやGraphQLとの連携を容易にする
* 初期登録ユーザー(OWNER)をTenant作成時に一括登録することで、別途での管理を要さずシンプルな設計を実現

---

### 📐 DTO クラスについて

認証機構で利用する DTO クラス名は、GraphQL スキーマ（SDL）と完全に一致させるために、以下の命名規則を採用します:

| クラス名           | 用途                                          |
| -------------- | ------------------------------------------- |
| `LoginInput`   | ログイン要求（email, password）                     |
| `LoginPayload` | レスポンス（accessToken, refreshToken, expiresIn） |

### 採用理由

1. **型安全・仕様ドリブン**
    * GraphQL SDL を唯一の真実源とし、クラス名をスキーマに揃えることで開発ミスを防止

2. **Codegen ツール親和性**
    * スキーマ変更 → 自動生成クラスの命名変更フローを CI に組み込みやすく、生産性と保守性を向上

3. **開発効率向上**
    * 手動マッピングコードを削減し、Resolver 実装をシンプルに保てる

---

## 🧱 DDDレイヤ構成

| レイヤ              | 主な責務と構成要素                                        | 例 (オーガナイザー認証)                                                       |
| ---------------- |--------------------------------------------------| ------------------------------------------------------------------- |
| Presentation 層   | GraphQL APIのログイン処理、DTOとの繋ぎ込み                     | `OrganizerLoginResolver`, `LoginInput`, `LoginPayload`                    |
| Application 層    | 認証ユースケースの制御、JWT発行処理                              | `OrganizerAuthService`, `JwtIssuer`                                 |
| Domain 層         | Organizerエンティティ、ValueObject、Repository interface | `Organizer`, `EmailAddress`, `OrganizerRole`, `OrganizerRepository` |
| Infrastructure 層 | JPA実装、JWT発行ユーティリティ、Security設定                    | `OrganizerJpaEntity`, `JwtTokenProvider`, `OrganizerSecurityConfig` |

## 🖼 テンプレート構成(View)

対象外（JWT認証 + SPA/GraphQLを前提とするためThymeleafなし）

## 📜 スキーマ構成 (SDL)

```graphql
input LoginInput {
  email: String!
  password: String!
}

type LoginPayload {
  accessToken: String!
  refreshToken: String
  expiresIn: Int!
  tenantId: ID!
  role: String!
}

type Mutation {
  loginOrganizer(input: LoginInput!): LoginPayload!
  refreshToken(token: String!): LoginPayload!
}
```
ポイント: クラス名を GraphQL SDL と完全一致させ、graphql-kotlin や Apollo Codegen との連携がシームレスに。


## 🔐 イベント主催者JWT認証の責務マッピング

| レイヤ            | 責務概要                                 | 代表的なクラス / 機能                                                        |
| -------------- | ------------------------------------ | ------------------------------------------------------------------- |
| Presentation   | ログイン情報の受け取り、GraphQLレシーバの構成           | `OrganizerLoginResolver`, `LoginRequestDTO`                         |
| Application    | Organizerの找続、証明情報(JWT payload)の生成    | `OrganizerAuthService`, `JwtIssuer`                                 |
| Domain         | Organizer情報の保持、検索ロジックの提供             | `Organizer`, `EmailAddress`, `OrganizerRole`                        |
| Infrastructure | OrganizerデータのJPA管理、JWT発行 / 検証ロジックの実装 | `OrganizerJpaRepository`, `JwtTokenProvider`, `SecurityFilterChain` |

## 🧩 レイヤ構成図

```mermaid
graph TD

%% Presentation層
UI[SPA / GraphQL Client] --> Resolver[Presentation層: OrganizerLoginResolver]
Resolver --> DTO[DTO: LoginInput, Payload]

%% Application層
Resolver --> AuthService[Application層: OrganizerAuthService]
AuthService --> JwtIssuer[JwtIssuer - JWT発行]

%% Domain層
AuthService --> RepositoryIF[Domain層: OrganizerRepository]
RepositoryIF --> DomainEntity[Organizer - Entity]
DomainEntity --> Email[EmailAddress - VO]
DomainEntity --> Role[OrganizerRole - Enum]

%% Infrastructure層
RepositoryIF --> JpaRepository[Infrastructure層: OrganizerJpaRepository]
JpaRepository --> SpringDataRepo[OrganizerSpringDataRepository]
JpaRepository --> Mapper[OrganizerMapper]
Mapper --> JpaEntity[OrganizerJpaEntity]
JpaEntity --> DB[(PostgreSQL)]

JwtIssuer --> JwtUtil[JwtTokenProvider]
SecurityConfig[OrganizerSecurityConfig] --> FilterChain[SecurityFilterChain - /organizer]
```

## 📋 各レイヤの役割一覧

| レイヤ             | パッケージ例                                        | クラス / コンポーネント名                                | 役割概要                                      |
| --------------- | --------------------------------------------- |-----------------------------------------------| ----------------------------------------- |
| Presentation層   | `presentation.organizer.auth`                 | `OrganizerLoginResolver`                      | GraphQLの認証リゾルバ。メール・パスワードを受け取り、Serviceに委譲  |
| Presentation層   | `presentation.organizer.auth.dto`             | `LoginInput`, `LoginPayload`                  | GraphQLでの入力・出力DTO（後続のJWTレスポンス含む）          |
| Application層    | `application.organizer.service`               | `OrganizerAuthService`                        | Organizerの検索、認証判定、およびUserDetailsへの変換を担う   |
| Application層    | `application.organizer.service.jwt`           | `JwtIssuer`, `JwtPayloadFactory`              | 認証済みユーザーに対してトークンを発行。認可クレームの整形も行う          |
| Domain層         | `domain.organizer.entity`                     | `Organizer`                                   | Organizerの業務モデル。メール・ロール・テナントIDを保持         |
| Domain層         | `domain.organizer.valueobject`                | `EmailAddress`, `OrganizerRole`               | メールやロールを型安全に保持・検証するための値オブジェクト             |
| Domain層         | `domain.organizer.repository`                 | `OrganizerRepository`                         | Organizer永続化の抽象定義。インフラ実装とは非依存             |
| Infrastructure層 | `infrastructure.persistence.organizer`        | `OrganizerJpaRepository`, `OrganizerMapper`   | Domain ↔ JPA 変換、およびRepositoryの具象実装        |
| Infrastructure層 | `infrastructure.persistence.organizer.entity` | `OrganizerJpaEntity`                          | JPAエンティティ（テーブルとのマッピング）                    |
| Infrastructure層 | `infrastructure.security.jwt`                 | `JwtTokenProvider`, `JwtAuthenticationFilter` | JWTの発行・検証、HTTPリクエストに対する認可制御を実装            |
| Infrastructure層 | `infrastructure.security.config`              | `OrganizerSecurityConfig`                     | `/organizer/**` 用の SecurityFilterChain 設定 |

## ✅ 各レイヤ別ファイルの必要性と理由

| 層               | クラス / コンポーネント名                              | 必要性分類    | 理由                                                      |
| --------------- |---------------------------------------------| -------- | ------------------------------------------------------- |
| Presentation層   | `OrganizerLoginResolver`                    | ⭐️ 必須    | GraphQL経由でのログイン要求を受け取り、認証ユースケースに委譲する役割                  |
| Presentation層   | `LoginInput`, `LoginPayload`                | ⭐️ 必須    | GraphQLでの入力（メール・パスワード）と出力（JWTトークン）を構造化してやり取りするため        |
| Application層    | `OrganizerAuthService`                      | ⭐️ 必須    | ユーザー検索・認証処理を担い、Spring Security 連携および JWT生成ロジックを制御       |
| Application層    | `JwtIssuer`, `JwtPayloadFactory`            | ⭐️ 必須    | ロール・テナント・サブジェクトなどの情報をJWTとして発行するためのユーティリティ               |
| Domain層         | `Organizer`                                 | ⭐️ 必須    | メール・ロール・テナントIDなど認証・認可に必要な属性を定義したドメインエンティティ              |
| Domain層         | `EmailAddress`, `OrganizerRole`             | 🧩 型安全志向 | 値の整合性（空文字・メール形式等）を保証するためのValueObject。ロールの定義も列挙体で管理      |
| Domain層         | `OrganizerRepository`                       | 🧩 DDD構成 | 永続化技術に依存しない設計とし、アプリケーション層からドメインに依存をとどめるため               |
| Infrastructure層 | `OrganizerJpaRepository`, `OrganizerMapper` | ⭐️ 必須    | JPA + Spring Data により永続化処理を担う具象実装と、Entity ↔ Domainの変換責務 |
| Infrastructure層 | `OrganizerJpaEntity`                        | ⭐️ 必須    | テーブルとの対応関係を明示し、UUID, Email, Role などのDBカラムにマッピング         |
| Infrastructure層 | `JwtTokenProvider`                          | ⭐️ 必須    | JWTトークンの発行・検証を担う中核コンポーネント                               |
| Infrastructure層 | `JwtAuthenticationFilter`                   | ⭐️ 必須    | HTTPリクエストにおけるAuthorizationヘッダを検査し、認可処理を行うフィルタ           |
| Infrastructure層 | `OrganizerSecurityConfig`                   | ⭐️ 必須    | `/organizer/**` のリクエストに対するSecurityFilterChainの設定        |

## 🧩 クラス間の関係

```mermaid
classDiagram

%% ==== Domain Layer ====
class Organizer {
  - EmailAddress email
  - OrganizerRole role
  - UUID tenantId
}

class EmailAddress {
  - String value
  + validate(): void
}

class OrganizerRepository {
  <<interface>>
  + findByEmail(email: EmailAddress): Organizer?
}

%% ==== Application Layer ====
class OrganizerAuthService {
  + authenticate(email: String, password: String): JwtToken
}

class JwtIssuer {
  + issueToken(organizer: Organizer): String
}

%% ==== Presentation Layer ====
class OrganizerLoginResolver {
  + login(email: String, password: String): LoginPayload
}

class LoginInput {
  + String email
  + String password
}

class LoginPayload {
  + String accessToken
  + String? refreshToken
  + Int expiresIn
  + ID tenantId
  + String role
}

%% ==== Infrastructure Layer ====
class OrganizerJpaEntity {
  - String email
  - String passwordDigest
  - String role
  - UUID tenantId
  + toDomain(): Organizer
}

class OrganizerJpaRepository {
  + findByEmail(email: EmailAddress): OrganizerJpaEntity?
}

class OrganizerMapper {
  + toDomain(entity: OrganizerJpaEntity): Organizer
  + toEntity(domain: Organizer): OrganizerJpaEntity
}

class JwtTokenProvider {
  + createToken(claims: Map<String, Any>): String
  + validateToken(token: String): Boolean
}

class JwtAuthenticationFilter {
  + doFilter(request, response, chain)
}

class OrganizerSecurityConfig {
  + securityFilterChain(http: HttpSecurity): SecurityFilterChain
}

%% ==== Relationships ====
Organizer --> EmailAddress
OrganizerAuthService --> OrganizerRepository
OrganizerAuthService --> Organizer
OrganizerAuthService --> JwtIssuer
OrganizerLoginResolver --> OrganizerAuthService
OrganizerLoginResolver --> LoginInput
OrganizerLoginResolver --> LoginPayload
OrganizerRepository <|.. OrganizerJpaRepository
OrganizerJpaRepository --> OrganizerMapper
OrganizerMapper --> Organizer
OrganizerMapper --> OrganizerJpaEntity
OrganizerJpaEntity --> EmailAddress
JwtIssuer --> JwtTokenProvider
JwtAuthenticationFilter --> JwtTokenProvider
OrganizerSecurityConfig --> JwtAuthenticationFilter
```

## 📘 補足: GraphQLを採用する理由

JWT認証を前提とした場合、REST APIと比べてGraphQLを採用することには次のような利点があります：

| 観点                | GraphQL（+JWT認証）                                   | REST API（+JWT認証）                  |
| ----------------- | ------------------------------------------------- | --------------------------------- |
| 🎯 **柔軟なデータ取得**   | クライアントが必要なフィールドだけを指定可能。過不足のない取得が可能。               | エンドポイントごとに固定レスポンス。不要なデータも含まれがち。   |
| 🔗 **1リクエスト複数操作** | 単一クエリで複数のエンティティを横断的に取得できる                         | リソース単位でエンドポイント分割。複数回リクエストが必要になる   |
| 📐 **型安全なスキーマ**   | JWTから抽出した `role`, `tenantId` をGraphQLスキーマで制御可能    | エンドポイント単位の処理が中心。型レベルの認可制御は自前実装が必要 |
| 🛡 **認可粒度の統一**    | Fieldレベルの権限制御が可能。`@PreAuthorize` や自作directiveで対応可 | メソッド単位でしか認可しにくく、粒度が粗くなりがち         |
| 🧾 **ドキュメント自動生成** | スキーマ＝仕様。GraphiQLやPlaygroundで直感的な探索が可能             | Swagger等を併用する必要があり、実装と乖離する場合も     |
| 🌐 **SPAとの親和性**   | クライアント状態に応じた効率的なデータ取得（Apollo等との連携）                | 複数APIをコールし、状態同期が煩雑になりやすい          |

これにより、Organizerドメインにおいては以下のようなメリットが得られます：

* 必要な情報だけを効率的に取得でき、API設計がユースケース単位で柔軟に
* テナント情報やロールによる認可をGraphQLスキーマやResolver内で統一的に適用可能
* 認可構成をコード＋スキーマで可視化しやすく、保守性が高い

---

## 📁 ファイルツリー

```plaintext
src/main/kotlin/com/example/kteventsaas/
├── presentation/
│   └── organizer/
│       └── auth/
│           ├── OrganizerLoginResolver.kt
│           └── dto/
│               ├── LoginInput.kt
│               └── LoginPayload.kt
├── application/
│   └── organizer/
│       └── service/
│           ├── OrganizerAuthService.kt
│           └── jwt/
│               ├── JwtIssuer.kt
│               └── JwtPayloadFactory.kt
├── domain/
│   └── organizer/
│       ├── entity/
│       │   └── Organizer.kt
│       ├── valueobject/
│       │   ├── EmailAddress.kt
│       │   └── OrganizerRole.kt
│       └── repository/
│           └── OrganizerRepository.kt
├── infrastructure/
│   ├── persistence/
│   │   └── organizer/
│   │       ├── OrganizerJpaRepository.kt
│   │       ├── OrganizerSpringDataRepository.kt
│   │       ├── entity/
│   │       │   └── OrganizerJpaEntity.kt
│   │       └── mapper/
│   │           └── OrganizerMapper.kt
│   └── security/
│       ├── config/
│       │   └── OrganizerSecurityConfig.kt
│       └── jwt/
│           ├── JwtTokenProvider.kt
│           └── JwtAuthenticationFilter.kt
└── resources/
    └── graphql/
        └── organizer/
            └── schema.graphqls
```
