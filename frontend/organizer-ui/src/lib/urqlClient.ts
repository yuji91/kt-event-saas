import { createClient, fetchExchange } from "urql";

export const client = createClient({
  url: "/graphql", // 開発環境ではViteのproxy設定を有効にするため相対パスを使用（本番は環境変数で切替予定）
  exchanges: [fetchExchange],
  fetchOptions: () => {
    const token = localStorage.getItem("accessToken");
    return {
      headers: { Authorization: token ? `Bearer ${token}` : "" },
    };
  },
});
