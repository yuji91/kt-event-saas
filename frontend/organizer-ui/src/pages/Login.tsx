import { useLoginOrganizerMutation } from '../graphql/generated';
import { LoginForm } from "../components/LoginForm";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export const Login = () => {
  const [, login] = useLoginOrganizerMutation();
  const { login: saveToken } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (email: string, password: string) => {
    const result = await login({ input: { email, password } });
    const data = result.data?.loginOrganizer;

    if (data?.accessToken) {
      saveToken({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken ?? undefined,
        tenantId: data.tenantId,
        email: data.email,
        role: data.role,
      });
      navigate("/dashboard");
    } else {
      // TODO: ErrorHandling
      console.error("Login failed", result.error);
    }
  };

  return <LoginForm onSubmit={handleLogin} />;
};
