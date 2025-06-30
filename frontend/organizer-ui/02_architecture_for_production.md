# 🧪 Organizer UI – 実務レベル対応に向けた改善方針

本ドキュメントでは、**現状のシンプルな構成から、実務水準への引き上げを行うための改善方針**を整理しています。
- 現状の構成: `React` + `Vite` + `TypeScript` + `urql` + `Chakra UI` + `GraphQL` を用いたSPA構成

---

## 🧱 現状の技術スタック（開発効率重視）

| 項目 | 内容 |
|------|------|
| フロント | React + Vite + TypeScript |
| UIライブラリ | Chakra UI |
| GraphQLクライアント | urql |
| 認証方式 | JWT + LocalStorage |
| ルーティング | react-router-dom |

---

## 🚧 実務との主な差分

| 項目 | 現状 | 実務水準 | 差分レベル |
|------|------|----------|-------------|
| SSR/SSG | ❌ 未対応 | ✅ Next.js等で対応 | ★★★★☆ |
| 認証 | JWT + LocalStorage | HttpOnly Cookie + Silent Refresh | ★★★★★ |
| 再認証 | ❌ 手動再ログイン | ✅ 自動リフレッシュ | ★★★★☆ |
| GraphQLクライアント | urql | Apollo (主流) | ★★★☆☆ |
| 状態管理 | Zustand | React Query などと併用 | ★★★☆☆ |
| ディレクトリ構成 | 層別構成 | 機能別構成（Feature-based） | ★★★★☆ |
| CI/CD | なし | GitHub Actions + Vercel など | ★★☆☆☆ |
| 監視 | なし | Sentry等による運用監視 | ★★☆☆☆ |

---

## 🛠 改善方針（Vite構成を維持しつつ後付け対応）

### 1. 認証の改善（HttpOnly Cookie + Silent Refresh）

- `accessToken` をサーバー側で `HttpOnly Cookie` に設定（`Set-Cookie`ヘッダー）
- フロントでは `credentials: "include"` を使って自動送信
- `/refresh` エンドポイントでアクセストークンの再発行を行う

```ts
const client = createClient({
  url: '/graphql',
  fetchOptions: {
    credentials: 'include',
  },
});
```
→ Zustand や React Context にて再認証を管理：

```ts
useEffect(() => {
  fetch("/api/auth/refresh", { credentials: "include" })
    .then(res => res.json())
    .then(data => setAccessToken(data.accessToken));
}, []);
```

### 2. GraphQL クライアント（Apollo or urql強化）
- urqlのまま継続する場合
  - authExchange でトークンの再取得を実装
  - GraphQL Codegen による型安全対応を維持
  - Apollo Clientへの切り替えも選択肢（学習コスト増だがエコシステム充実）

### 3. 機能別ディレクトリ構成への移行（Feature-based）
```plaintext
frontend/
└── organizer-ui/
    ├── public/
    └── src/
        ├── features/
        │   ├── auth/              # ログイン・再認証など
        │   ├── dashboard/         # ダッシュボード機能
        │   └── organizer/         # 主催者情報管理
        ├── shared/
        │   ├── components/        # 汎用コンポーネント
        │   ├── lib/               # GraphQLクライアント、ストレージ制御
        │   └── theme/             # Chakra UI テーマ設定
        ├── App.tsx
        └── main.tsx
```
→ ドメインごとの責務分離で、スケーラビリティと保守性が向上

### 4. 状態管理：React Queryとの併用
APIのキャッシュ・リフェッチ管理は React Query

UI用の軽量な状態管理は Zustand に限定するのが実務的

```ts
const { data, isLoading } = useQuery(['currentUser'], fetchCurrentUser);
```

### 5. セキュリティ強化：XSS対策
- `index.html`に`Content-Security-Policy（CSP）`を設定

```html
<meta http-equiv="Content-Security-Policy" content="default-src 'self'; script-src 'self';">
```
→ LocalStorageからの移行 or accessToken のDOM露出回避


### 6. CI/CD・監視導入
| 項目                                   | ツール例                       |
| ------------------------------------ |----------------------------|
| `CI` | GitHub Actions             | 
| `CD` | Vercel / Cloudflare Pages  | 
| `監視` | Sentry（無料枠あり）        |        

→ Viteでもすぐに導入可能

---

## 🔚 結論
現状の軽量構成（React + Vite + urql + LocalStorage）は個人開発やPoCに適していますが、
実務レベルではセキュリティ・UX・保守性の観点から、段階的な改善が必要です。

ただし、Next.jsを使わずとも、すべて後付けで改善可能です。
最終的な導入判断は、プロジェクトの目的・規模・チーム構成に応じて選択可能です。

### 📌 補足（改善ステップの優先度）

| ステップ | 内容 | 優先度 |
| ------------------------------------ |----------------------------|----------------------------|
| ✅ 認証をHttpOnly  Cookieに移行 | せキュリティ改善 | ★★★★★ | 
| ✅ Silent Refresh対応 | UX改善 | ★★★★★ | 
| ✅ 状態管理をReact Query併用 | 保守性 | ★★★★☆ |
| ✅ ディレクトリ構成の見直し | スケーラビリティ | ★★★★☆ |
| ✅ GraphQLクライアント選定見直し | チーム適応 | ★★★☆☆ |
| ✅ CI/CD・監視導入 | 品質保証 | ★★☆☆☆ | 

## 🧱 改善後の技術スタック（実務対応向け）
| 項目            | 内容                                              |
| ------------- | ----------------------------------------------- |
| フロントエンド       | React + Vite + TypeScript                       |
| UIライブラリ       | Chakra UI                                       |
| 認証方式          | HttpOnly Cookie + Silent Refresh                |
| GraphQLクライアント | urql（authExchange適用）または Apollo Client（選択可）      |
| 状態管理          | React Query（データ取得・キャッシュ） + Zustand（UI状態）        |
| ディレクトリ構成      | 機能別構成（Feature-based）                            |
| セキュリティ        | Content-Security-Policy（CSP） + アクセストークンのDOM露出回避 |
| 再認証処理         | `/api/auth/refresh` による自動アクセストークン更新             |
| CI            | GitHub Actions                                  |
| CD            | Vercel または Cloudflare Pages                     |
| 監視・エラートラッキング  | Sentry（無料枠あり）                                   |
| 型安全対応         | GraphQL Code Generator による自動生成型                 |

