import { useAuth } from "@/contexts/AuthContext";
import { useUserRole } from "@/hooks/useAuthHooks";
import { useLocation, Link, useNavigate } from "react-router-dom";
import { UserRole } from "@/types/auth";
import {
  LayoutDashboard,
  FileText,
  Briefcase,
  MessageSquare,
  Users,
  BarChart3,
  LogOut,
  Shield,
  User,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
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
    icon: BarChart3,
    label: "Reports",
    href: "/reports",
    roles: [UserRole.ADMIN, UserRole.CEO, UserRole.CFO, UserRole.AUDITOR],
  },
  {
    icon: Users,
    label: "User Management",
    href: "/admin",
    roles: [UserRole.ADMIN],
  },
  {
    icon: Shield,
    label: "Register User",
    href: "/register",
    roles: [UserRole.ADMIN],
  },
];

export function Sidebar() {
  const { user, logout } = useAuth();
  const userRole = useUserRole();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate("/login");
  };

  const visibleItems = navItems.filter((item) => {
    if (!item.roles) return true;
    return userRole && item.roles.includes(userRole);
  });

  const userInitials =
    user?.name
      ?.split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase() || "U";

  return (
    <div className="flex h-full w-64 flex-col bg-background border-r">
      {/* Logo */}
      <div className="flex h-14 items-center border-b px-4">
        <h1 className="text-lg font-semibold">E-FileConnect</h1>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-1 p-4">
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
              <Icon className="h-4 w-4" />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>

      {/* User Profile */}
      <div className="border-t p-4">
        <div className="flex items-center space-x-3 mb-3">
          <Avatar className="h-8 w-8">
            <AvatarFallback>{userInitials}</AvatarFallback>
          </Avatar>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{user?.name}</p>
            <p className="text-xs text-muted-foreground truncate">
              {user?.role}
            </p>
          </div>
        </div>

        <div className="space-y-1">
          <Link
            to="/profile"
            className="flex items-center space-x-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-accent hover:text-accent-foreground w-full"
          >
            <User className="h-4 w-4" />
            <span>Profile</span>
          </Link>

          <Button
            variant="ghost"
            size="sm"
            onClick={handleLogout}
            className="flex items-center space-x-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-accent hover:text-accent-foreground w-full justify-start"
          >
            <LogOut className="h-4 w-4" />
            <span>Logout</span>
          </Button>
        </div>
      </div>
    </div>
  );
}
