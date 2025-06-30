import { useEffect, useState } from "react";
import { useCurrentOrganizerQuery } from "../graphql/generated";

type UserInfo = {
  accessToken: string;
  refreshToken?: string;
  tenantId: string;
  email: string;
  role: string;
};

export const useAuth = () => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<UserInfo | null>(null);
  const [initialized, setInitialized] = useState(false);

  // 初回 localStorage から読み込み
  useEffect(() => {
    const stored = localStorage.getItem("userInfo");
    if (stored) {
      const parsed = JSON.parse(stored);
      setToken(parsed.accessToken);
      setUser(parsed);
    }
    setInitialized(true);
  }, []);

  // 🔍 サーバーでトークン検証（token があるときのみ実行）
  const [{ data, error }] = useCurrentOrganizerQuery({
    pause: !token,
  });

  useEffect(() => {
    if (error?.message.includes("Unauthorized")) {
      logout(); // トークンが無効ならログアウト
    } else if (data?.currentOrganizer) {
      // 🔁 サーバーの正しい情報で user を上書き（email, role, tenantId）
      setUser((prev) =>
          prev
              ? {
                ...prev,
                email: data.currentOrganizer.email,
                role: data.currentOrganizer.role,
                tenantId: data.currentOrganizer.tenantId,
              }
              : null
      );
    }
  }, [data, error]);

  const login = (userInfo: UserInfo) => {
    localStorage.setItem("userInfo", JSON.stringify(userInfo));
    localStorage.setItem("accessToken", userInfo.accessToken);
    setToken(userInfo.accessToken);
    setUser(userInfo);
  };

  const logout = () => {
    localStorage.removeItem("userInfo");
    localStorage.removeItem("accessToken");
    setToken(null);
    setUser(null);
  };

  return { token, user, login, logout, isAuthenticated: !!token, initialized };
};
