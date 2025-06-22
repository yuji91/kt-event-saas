import { ChakraProvider } from "@chakra-ui/react";
import { Provider as UrqlProvider } from "urql";
import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import theme from "./theme";
import { client } from "./lib/urqlClient";

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <UrqlProvider value={client}>
            <ChakraProvider theme={theme}>
                <App />
            </ChakraProvider>
        </UrqlProvider>
    </React.StrictMode>
);
