import { useState } from "react";
import { Box, Input, Button, VStack } from "@chakra-ui/react";

type Props = {
  onSubmit: (email: string, password: string) => void;
};

export const LoginForm = ({ onSubmit }: Props) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  return (
    <Box maxW="md" mx="auto" mt="10">
      <VStack spacing={4}>
        <Input
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />
        <Button colorScheme="blue" onClick={() => onSubmit(email, password)}>
          Login
        </Button>
      </VStack>
    </Box>
  );
};
