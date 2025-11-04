import { useDashboard } from "@/hooks/useDashboard";
import { useAuthStore, useUserRole } from "@/stores/authStore";
import { StatsCard } from "@/components/common/StatsCard";
import {
  FileText,
  CheckCircle,
  XCircle,
  Clock,
  Briefcase,
  MessageSquare,
  TrendingUp,
} from "lucide-react";
import { UserRole } from "@/types/auth";
import { ApproverOnly } from "@/components/auth/RoleGuard";

export function DashboardPage() {
  const { user } = useAuthStore();
  const userRole = useUserRole();
  const { data: dashboardData, loading } = useDashboard();

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Welcome back, {user?.name}!
        </h1>
        <p className="text-muted-foreground">
          Here's what's happening with your documents and cases today.
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="Total Documents"
          value={dashboardData?.totalDocuments || 0}
          description="All documents in system"
          icon={FileText}
        />

        <ApproverOnly>
          <StatsCard
            title="Pending Approval"
            value={dashboardData?.pendingDocuments || 0}
            description="Awaiting your review"
            icon={Clock}
            className="border-orange-200 bg-orange-50"
          />
        </ApproverOnly>

        <StatsCard
          title="Approved"
          value={dashboardData?.approvedDocuments || 0}
          description="Successfully approved"
          icon={CheckCircle}
          className="border-green-200 bg-green-50"
        />

        <StatsCard
          title="Active Cases"
          value={dashboardData?.activeCases || 0}
          description="Currently in progress"
          icon={Briefcase}
        />
      </div>

      {/* Role-specific content */}
      {(userRole === UserRole.CEO || userRole === UserRole.CFO) && (
        <div className="grid gap-4 md:grid-cols-2">
          <StatsCard
            title="Total Cases"
            value={dashboardData?.totalCases || 0}
            description="All cases in system"
            icon={TrendingUp}
          />
          <StatsCard
            title="Unread Messages"
            value={dashboardData?.unreadCommunications || 0}
            description="New communications"
            icon={MessageSquare}
          />
        </div>
      )}

      {/* Quick Actions or Recent Activity could go here */}
    </div>
  );
}
