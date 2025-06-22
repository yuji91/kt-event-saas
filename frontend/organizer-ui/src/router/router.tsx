import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Login } from "../pages/Login";

const router = createBrowserRouter([
  { path: "/", element: <Login /> }
]);

export const AppRouter = () => <RouterProvider router={router} />;
