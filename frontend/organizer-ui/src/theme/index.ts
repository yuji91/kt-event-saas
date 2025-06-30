import { extendTheme } from "@chakra-ui/react";

const config = {
    initialColorMode: "light" as const,
    useSystemColorMode: false,
};

const theme = extendTheme({ config });

export default theme;
