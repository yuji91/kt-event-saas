import { createBrowserRouter, RouterProvider, Navigate } from "react-router-dom";
import { Login } from "../pages/Login";
import { Dashboard } from "../pages/Dashboard";
import { useAuth } from "../hooks/useAuth";

const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { isAuthenticated, initialized } = useAuth();

    if (!initialized) {
        return <div>Loading...</div>; // またはローディングスピナー
    }

  return isAuthenticated ? children : <Navigate to="/" />;
};

const router = createBrowserRouter([
  { path: "/", element: <Login /> },
  {
    path: "/dashboard",
    element: (
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
    ),
  },
]);

export const AppRouter = () => <RouterProvider router={router} />;
