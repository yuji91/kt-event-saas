import { useMutation } from "urql";
import { LOGIN_ORGANIZER } from "../graphql/loginOrganizer.gql";
import { LoginForm } from "../components/LoginForm";
import { useAuth } from "../hooks/useAuth";

export const Login = () => {
  const [, login] = useMutation(LOGIN_ORGANIZER);
  const { login: saveToken } = useAuth();

  const handleLogin = async (email: string, password: string) => {
    const result = await login({ input: { email, password } });
    if (result.data?.loginOrganizer.accessToken) {
      saveToken(result.data.loginOrganizer.accessToken);
      // TODO: Navigate to dashboard
    }
  };

  return <LoginForm onSubmit={handleLogin} />;
};
