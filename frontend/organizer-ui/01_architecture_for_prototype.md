# 🧑‍💼 イベント主催者用、Organizer UI (React + Chakra UI)

## 🧱 技術スタック（初期開発向け）

- `React`+`TypeScript`+`Vite`による`SPA`構成と`Chakra UI`、`GraphQL`クライアントは`urql`を使用

| 技術         | 用途                     |
|--------------|--------------------------|
| React        | SPA構築                  |
| Chakra UI    | UIライブラリ・デザイン統一 |
| urql         | GraphQLクライアント      |
| Vite         | ビルドツール              |
| TypeScript   | 型安全                    |

---

## ⚛️ フレームワーク構成
- React（Vite）を使用し、Next.js は使用しません。
  - SPA構成を重視し、SSRやディレクトリルーティングは不採用
  - ビルド・デプロイのシンプルさを優先
  - ルーティングは`react-router-dom`を使用し、認証ガードや条件付き遷移を明示的に制御

---

## 🌐 GraphQL クライアント（`urql`）の選定理由

- 本プロジェクトでは、**技術要件の充足度（JWT対応・型安全・キャッシュ制御）** と  
  **学習コスト・開発速度のバランス** を考慮し、`urql` を採用しています。

### 🔧 1. 軽量かつ柔軟な構成
- exchange ベースのミドルウェア設計により、認証やキャッシュ制御などを 必要最小限の構成で実現可能
- authExchange を使うことで、アクセストークンの動的注入や再認証処理をシンプルに記述できる

### 🧬 2. 型安全な開発との親和性
- GraphQL Code Generator との組み合わせにより、型安全なクエリ定義・フック生成が可能
- useQuery, useMutation フックが TypeScriptベースで直感的に利用できる

### 🔁 3. 開発体験のバランスが良い
- Apollo Client に比べて 学習コストや構成が軽量で、初期導入がスムーズ
- Relay に比べて 抽象化が浅く、自由度が高いため、小〜中規模SPAで柔軟に設計可能

### 🎯 4. 本プロジェクトとの適合性
- 認証済みSPA（Organizer UI）において、SEO・SSR不要な構成に対して urql は最適
- JWT を使った GraphQL 認証、GraphQL スキーマ駆動開発との相性が良く、実装・保守の手間を抑えられる

### 📊他クライアントとの比較

| クライアント名     | 技術要件充足度 | 学習コスト・速度 | コメント                     |
|------------------|--------------|----------------|------------------------------|
| urql             | ⭐⭐⭐⭐☆       | ⭐⭐⭐⭐☆         | 軽量・柔軟、認証付きSPAに最適 |
| Apollo Client    | ⭐⭐⭐⭐⭐       | ⭐⭐☆☆☆         | 高機能だが構成が重く複雑      |
| Relay            | ⭐⭐⭐⭐☆       | ☆☆☆☆☆         | 学習コストが高くFB向け設計     |
| graphql-request  | ⭐⭐☆☆☆       | ⭐⭐⭐⭐⭐         | 単純な実装に最適、拡張性に課題  |

---

## 🧩 GraphQL API の URL 設定方針（開発/本番の切替）
- 開発環境では`urql`クライアントの`url`は相対パス`/graphql`を使用して開発速度を優先する
- これは`Vite`の`server.proxy`機能を活用しバックエンドにリクエストを中継する構成

```ts
// urqlClient.ts（開発環境）
url: "/graphql" // 開発環境ではViteのproxy設定を有効にするため相対パスを使用（本番は環境変数で切替予定）
```
※ 将来的に本番環境では、環境変数（例：VITE_API_URL=https://api.example.com/graphql）を用いて URL を切り替える構成へ拡張可能。

---

## 🛡️ トークン保存とセキュリティ
アクセストークンの保存先は`LocalStorage`を採用しています。  
他の選択肢と比較した上で、開発効率と技術要件のバランスを考慮した選定です。  

### 📊 トークン保存場所の比較
| 保存場所                  | 特徴                                 | メリット                        | デメリット                        | 🛡 追加対応の要否                                     | 💰 追加対応のコスト |
| --------------------- | ---------------------------------- | --------------------------- | ---------------------------- | ---------------------------------------------- |-------------|
| **LocalStorage**      | `window.localStorage` に保存          | ✅ 実装が簡単<br>✅ リロードでも保持される    | ❌ XSSの影響を受ける<br>❌ 全タブ共有      | 🔶 **CSP導入**, DOMへのトークン埋め込み回避, `removeItem` 明示 | ⭐⭐☆☆☆       |
| **SessionStorage**    | タブごとに分離された `window.sessionStorage` | ✅ タブ閉じると自動破棄<br>✅ XSS耐性は同程度 | ❌ タブを閉じると消える<br>❌ 他タブと共有不可   | 🔶 LocalStorageと同様に **CSPや初期化設計**が必要           | ⭐⭐☆☆☆       |
| **Memory（変数）**        | アプリ実行中の変数に保持（例：Zustandなど）          | ✅ XSSに強い<br>✅ 実装が自由で高速      | ❌ リロードで消失<br>❌ F5で再ログインが必要   | 🔸 **再ログイン・自動再認証設計**, 状態の永続化フローを設計             | ⭐⭐⭐☆☆       |
| **Cookie (HttpOnly)** | `Secure + HttpOnly` Cookie（サーバー送信） | ✅ XSSから保護される<br>✅ 自動送信で便利   | ❌ CSRFリスクあり<br>❌ サーバー側の処理が複雑 | 🔺 **CSRF対策**, Secure属性, サーバー同梱処理, SameSite 設定 | ⭐⭐⭐⭐☆       |

✔  トークン保存場所の選定は将来的に切り替え可能なため「開発速度と構築のしやすさ」を優先する

---

### 🔐 最低限の XSS 対策（補足）
- `LocalStorage`使用に際して、以下のような最低限のセキュリティ対策を講じます。

| 対策内容               | 防御タイミング         | 対応するリスクの種類                 |
| ------------------ | --------------- | -------------------------- |
| **CSP の導入**        | XSSの「実行を防ぐ」前提   | 外部スクリプトやインラインスクリプトの実行をブロック |
| **DOM へのトークン露出回避** | XSSが起きた「後の被害防止」 | DOM 経由でトークン情報を盗まれるリスク      |

---

## 🗂️ ディレクトリ構成

```plaintext
organizer-ui/
├── public/                     # index.htmlなど
├── src/
│   ├── graphql/                 # GraphQLクエリ・ミューテーション定義
│   ├── components/             # UI部品（OrganizerInfoなど）
│   ├── hooks/                  # useAuthなどの認証ロジック
│   ├── lib/                    # urqlClientやストレージ制御
│   ├── pages/                  # 各ページ（Login / Dashboard）
│   ├── router/                 # React Router定義
│   ├── theme/                  # Chakra UIのテーマ設定（任意）
│   ├── App.tsx                 # ルーティング統括
│   └── main.tsx                # ChakraProvider + Router起動
├── .env                        # VITE_API_URLなど
├── vite.config.ts
└── package.json
```

---

## 🧩 必須バックエンドAPI（実装）  
本フロントエンドは、`Organizer`ドメイン向け`GraphQL API`に依存しています。  
正常に動作させるためには、以下のバックエンド構成が整っている必要があります。  

### 🔐 認証関連 API（GraphQL）

| 操作                                   | 説明                              |
| ------------------------------------ | ------------------------------- |
| `loginOrganizer(input: OrganizerLoginInput!)` | Email/Password によるログイン認証、JWT 発行 |
| `currentOrganizer`                   | アクセストークンに基づくログイン中ユーザー情報の取得      |

### 📜 GraphQL スキーマ（SDL）

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

type OrganizerInfo {
  email: String!
  role: String!
  tenantId: ID!
}

type Mutation {
  loginOrganizer(input: OrganizerLoginInput!): OrganizerLoginPayload!
}

type Query {
  currentOrganizer: OrganizerInfo!
}
```

---

## ⚙️ 前提となるバックエンド構成（JWT認証・DB）

### 🛡 JWT認証の構成要件
- `/graphql` へのリクエストに対して `JWT`フィルターが適用されていること
- `Authorization: Bearer <token>` 形式のトークンが検証されること
- `JWT`トークンに`sub`, `role`, `tenantId` などのクレームが含まれていること

---

### 🗃 データベース要件
- `organizers`テーブルに有効なユーザーレコードが存在すること（最低1件）
- 必要カラム：`email, password_digest, tenant_id, role`
- パスワードは BCrypt でハッシュ化されていること
