import { Box, Heading, Text, Button } from "@chakra-ui/react";
import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/"); // ✅ ログアウト後はログイン画面へリダイレクト
  };

  return (
      <Box p={6}>
        <Heading>Welcome</Heading>
        <Text>Role: {user?.role}</Text>
        <Text>Tenant ID: {user?.tenantId}</Text>

        <Button mt={4} colorScheme="red" onClick={handleLogout}>
          Logout
        </Button>
      </Box>
  );
};
