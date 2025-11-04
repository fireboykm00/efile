import { useAuthStore, useUserRole } from "@/stores/authStore";
import { useLocation, Link, useNavigate } from "react-router-dom";
import { UserRole } from "@/types/auth";
import {
  LayoutDashboard,
  FileText,
  Briefcase,
  MessageSquare,
  Users,
  BarChart3,
  Settings,
  LogOut,
  Shield,
  User,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { AuthService } from "@/services/authService";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";

interface NavItem {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  href: string;
  roles?: UserRole[];
}

const navItems: NavItem[] = [
  {
    icon: LayoutDashboard,
    label: "Dashboard",
    href: "/dashboard",
  },
  {
    icon: FileText,
    label: "Documents",
    href: "/documents",
  },
  {
    icon: Briefcase,
    label: "Cases",
    href: "/cases",
  },
  {
    icon: MessageSquare,
    label: "Communications",
    href: "/communications",
  },
  {
    icon: Users,
    label: "Users",
    href: "/users",
    roles: [UserRole.ADMIN, UserRole.IT],
  },
  {
    icon: Shield,
    label: "Audit Logs",
    href: "/audit",
    roles: [UserRole.ADMIN, UserRole.IT, UserRole.AUDITOR],
  },
  {
    icon: BarChart3,
    label: "Reports",
    href: "/reports",
    roles: [UserRole.ADMIN, UserRole.CEO, UserRole.CFO, UserRole.AUDITOR],
  },
  {
    icon: Settings,
    label: "Admin",
    href: "/admin",
    roles: [UserRole.ADMIN],
  },
];

export function Sidebar() {
  const { user, logout } = useAuthStore();
  const userRole = useUserRole();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await AuthService.logout();
    logout();
    navigate("/login");
  };

  const visibleItems = navItems.filter((item) => {
    if (!item.roles) return true;
    return userRole && item.roles.includes(userRole);
  });

  return (
    <aside className="flex h-screen w-64 flex-col border-r bg-card">
      {/* Logo/Brand */}
      <div className="flex h-16 items-center border-b px-6">
        <FileText className="h-6 w-6 text-primary" />
        <span className="ml-2 text-lg font-semibold">E-FileConnect</span>
      </div>

      {/* User Info */}
      <div className="border-b p-4">
        <div className="flex items-center space-x-3">
          <Avatar>
            <AvatarFallback className="bg-primary text-primary-foreground">
              {user?.name.charAt(0).toUpperCase()}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{user?.name}</p>
            <p className="text-xs text-muted-foreground truncate capitalize">
              {user?.role.toLowerCase()}
            </p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 p-4 overflow-y-auto">
        {visibleItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.href;

          return (
            <Link
              key={item.href}
              to={item.href}
              className={cn(
                "flex items-center space-x-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors",
                isActive
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
              )}
            >
              <Icon className="h-5 w-5" />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>

      {/* Footer Actions */}
      <div className="border-t p-4 space-y-2">
        <Link to="/profile">
          <Button variant="ghost" className="w-full justify-start">
            <User className="mr-3 h-5 w-5" />
            Profile
          </Button>
        </Link>
        <Button
          variant="ghost"
          className="w-full justify-start text-destructive hover:text-destructive hover:bg-destructive/10"
          onClick={handleLogout}
        >
          <LogOut className="mr-3 h-5 w-5" />
          Logout
        </Button>
      </div>
    </aside>
  );
}
