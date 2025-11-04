import { useAuth } from '@/contexts/AuthContext';
import { UserRole } from '@/types/auth';

// User data hooks
export const useUser = () => {
  const { user } = useAuth();
  return user;
};

export const useIsAuthenticated = () => {
  const { isAuthenticated } = useAuth();
  return isAuthenticated;
};

export const useAuthLoading = () => {
  const { isLoading } = useAuth();
  return isLoading;
};

export const useAuthError = () => {
  const { error } = useAuth();
  return error;
};

// Role-based hooks
export const useUserRole = (): UserRole | null => {
  const { user } = useAuth();
  return user?.role || null;
};

export const useHasRole = (role: UserRole): boolean => {
  const userRole = useUserRole();
  return userRole === role;
};

export const useHasAnyRole = (roles: UserRole[]): boolean => {
  const userRole = useUserRole();
  return userRole ? roles.includes(userRole) : false;
};

export const useIsAdmin = (): boolean => {
  return useHasRole(UserRole.ADMIN);
};

export const useIsExecutive = (): boolean => {
  return useHasAnyRole([UserRole.CEO, UserRole.CFO]);
};