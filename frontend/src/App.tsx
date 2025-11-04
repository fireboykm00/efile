import {
  BrowserRouter as Router,
  Routes,
  Route,
  Navigate,
} from "react-router-dom";
import { LoginPage } from "@/pages/LoginPage";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { useAuthStore } from "@/stores/authStore";
import { useEffect } from "react";
import { AuthService } from "@/services/authService";
import { DashboardPage } from "@/pages/DashboardPage";
import { DocumentsPage } from "@/pages/DocumentsPage";
import { CasesPage } from "@/pages/CasesPage";
import { AdminPage } from "@/pages/AdminPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { RegisterPage } from "@/pages/RegisterPage";
import { ErrorBoundary } from "@/components/common/ErrorBoundary";
import { Toaster } from "sonner";
import { DashboardLayout } from "@/components/layout/DashboardLayout";

function App() {
  const { isAuthenticated, login } = useAuthStore();

  // Check for existing authentication on app load
  useEffect(() => {
    const initializeAuth = async () => {
      const token = AuthService.getAccessToken();
      if (token && !isAuthenticated) {
        try {
          // Verify token is still valid by fetching user data
          const userData = await AuthService.getCurrentUser();
          login(userData.user);
        } catch {
          // Token is invalid, clear it
          AuthService.logout();
        }
      }
    };

    initializeAuth();
  }, [isAuthenticated, login]);

  return (
    <ErrorBoundary>
      <Router>
        <div className="App">
          <Routes>
            {/* Public routes */}
            <Route
              path="/login"
              element={
                isAuthenticated ? (
                  <Navigate to="/dashboard" replace />
                ) : (
                  <LoginPage />
                )
              }
            />

            <Route path="/register" element={isAuthenticated ? (
                  <Navigate to="/dashboard" replace />
                ): (<RegisterPage />)} />

            {/* Protected routes with layout */}
            <Route
              element={
                <ProtectedRoute>
                  <DashboardLayout />
                </ProtectedRoute>
              }
            >
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/documents" element={<DocumentsPage />} />
              <Route path="/cases" element={<CasesPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/admin" element={<AdminPage />} />
            </Route>

            {/* Default redirect */}
            <Route
              path="/"
              element={
                <Navigate
                  to={isAuthenticated ? "/dashboard" : "/login"}
                  replace
                />
              }
            />

            {/* Catch all route */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
          <Toaster position="top-right" />
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
