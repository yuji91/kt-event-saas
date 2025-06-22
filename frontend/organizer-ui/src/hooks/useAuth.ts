import { useEffect, useState } from "react";

export const useAuth = () => {
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    setToken(localStorage.getItem("accessToken"));
  }, []);

  const login = (accessToken: string) => {
    localStorage.setItem("accessToken", accessToken);
    setToken(accessToken);
  };

  const logout = () => {
    localStorage.removeItem("accessToken");
    setToken(null);
  };

  return { token, login, logout, isAuthenticated: !!token };
};
