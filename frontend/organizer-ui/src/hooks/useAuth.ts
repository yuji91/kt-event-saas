import { useEffect, useState } from "react";

type UserInfo = {
  accessToken: string;
  refreshToken?: string;
  tenantId: string;
  role: string;
};

export const useAuth = () => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<UserInfo | null>(null);
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem("userInfo");
    if (stored) {
      const parsed = JSON.parse(stored);
      setToken(parsed.accessToken);
      setUser(parsed);
    }
    setInitialized(true);
  }, []);

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
