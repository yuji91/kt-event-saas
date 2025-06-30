# Organizer UI README

このディレクトリには、Organizer UI の開発・設計に関するドキュメントを格納しています。

- 📄 [プロトタイプ向け構成](./01_architecture_for_prototype.md)  
  ⇒ React + Vite + urql を用いた軽量な SPA 開発構成（PoC・初期開発向け）

- 🧪 [実務対応向け構成](./02_architecture_for_production.md)  
  ⇒ Cookie認証や状態管理の改善、CI/CD対応など、実運用レベルを想定した改善方針

---

## 🚀 セットアップ時に必要なコマンド

```shell
# Voltaの導入とNode.js 20のインストール
curl https://get.volta.sh | bash
source ~/.bashrc   # or ~/.zshrc（必要な場合のみ）
volta install node@20
volta pin node@20

# Viteテンプレートでプロジェクト初期化
cd frontend/organizer-ui
npm create vite@latest . -- --template react-ts

# 必要ライブラリのインストール
npm install
npm install @chakra-ui/react @emotion/react @emotion/styled framer-motion
npm install urql graphql
npm install react-router-dom

# ディレクトリ構成の作成
mkdir -p src/{graphql,components,hooks,lib,pages,router,theme}
　
# graphql-codegen で型定義を自動生成するため実行
npm install -D \
  @graphql-codegen/cli \                         # Codegen CLI本体
  @graphql-codegen/typescript \                  # GraphQLスキーマ → TypeScript型定義
  @graphql-codegen/typescript-operations \       # クエリ・ミューテーション → 型化
  @graphql-codegen/typescript-urql               # urql用の useQuery/useMutation フック自動生成

npx graphql-codegen
```

## ⚙️ Node.js 環境の統一と Volta 採用理由

本プロジェクトでは、**Node.js バージョン管理ツールとして [Volta](https://volta.sh/) を採用**

### 🔧 Volta を使う理由

- `node`, `npm`, `yarn`などのバージョンを **プロジェクト単位で固定可能**
- `volta pin`により、**開発チーム・CI/CD・本番でのバージョンズレを防止**
- `PATH`の切り替えを自動で行うため、**nvm より高速・安定**
- クロスプラットフォーム（macOS / Linux / Windows）対応済

### 📦 Node.js 20 に固定した理由

- `Node.js 20`は **最新の LTS（Long-Term Support）版** であり、長期運用に適している
- `Vite`, `React 18+`, `urql`, `Chakra UI` など、主要ライブラリが`Node 20 `で十分に検証・対応済
- `--experimental-fetch` や `--watch` の正式化など、**開発者体験が向上**

### GraphQL Code Generator を使用する理由
- `src/graphql/generated.ts`が生成される
- `.graphql` ファイルから型定義・クエリ関数（useQuery/useMutation）を自動生成する
- 手動での型定義やgql定義ファイルの管理が不要となり、開発効率と保守性が向上

