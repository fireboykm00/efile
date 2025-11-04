import { useUserRole, useHasAnyRole } from "@/stores/authStore";
import { UserRole } from "@/types/auth";

interface RoleGuardProps {
  children: React.ReactNode;
  roles: UserRole[];
  fallback?: React.ReactNode;
}

export function RoleGuard({ children, roles, fallback = null }: RoleGuardProps) {
  const hasRole = useHasAnyRole(roles);

  if (!hasRole) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}

// Convenience components for specific roles
export function AdminOnly({ children, fallback }: Omit<RoleGuardProps, "roles">) {
  return (
    <RoleGuard roles={[UserRole.ADMIN]} fallback={fallback}>
      {children}
    </RoleGuard>
  );
}

export function ExecutiveOnly({ children, fallback }: Omit<RoleGuardProps, "roles">) {
  return (
    <RoleGuard roles={[UserRole.CEO, UserRole.CFO]} fallback={fallback}>
      {children}
    </RoleGuard>
  );
}

export function ApproverOnly({ children, fallback }: Omit<RoleGuardProps, "roles">) {
  return (
    <RoleGuard roles={[UserRole.ADMIN, UserRole.CEO, UserRole.CFO]} fallback={fallback}>
      {children}
    </RoleGuard>
  );
}

