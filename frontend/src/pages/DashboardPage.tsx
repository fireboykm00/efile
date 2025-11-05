import { useDashboard } from "@/hooks/useDashboard";
import { useAuth } from "@/contexts/AuthContext";
import { useUserRole } from "@/hooks/useAuthHooks";
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
  const { user } = useAuth();
  const userRole = useUserRole();
  const { data: dashboardData, loading, error } = useDashboard();

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

  if (error) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-red-600">
            Error loading dashboard: {error.message}
          </p>
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
          icon={FileText}
          description="All documents in system"
        />
        <StatsCard
          title="Approved"
          value={dashboardData?.approvedDocuments || 0}
          icon={CheckCircle}
          description="Successfully approved"
          className="text-green-600"
        />
        <StatsCard
          title="Pending"
          value={dashboardData?.pendingDocumentsCount || 0}
          icon={Clock}
          description="Awaiting approval"
          className="text-yellow-600"
        />
        <StatsCard
          title="Rejected"
          value={dashboardData?.rejectedDocuments || 0}
          icon={XCircle}
          description="Rejected documents"
          className="text-red-600"
        />
      </div>

      {/* Role-specific content */}
      <ApproverOnly>
        <div className="grid gap-4 md:grid-cols-2">
          <StatsCard
            title="Active Cases"
            value={dashboardData?.activeCases || 0}
            icon={Briefcase}
            description="Cases requiring attention"
          />
          <StatsCard
            title="Active Cases"
            value={dashboardData?.activeCases || 0}
            icon={MessageSquare}
            description="Cases in progress"
          />
        </div>
      </ApproverOnly>

      {/* Executive Dashboard */}
      {userRole && [UserRole.CEO, UserRole.CFO].includes(userRole) && (
        <div className="mt-6">
          <h2 className="text-2xl font-bold mb-4">Executive Overview</h2>
          <div className="grid gap-4 md:grid-cols-3">
            <StatsCard
              title="Monthly Growth"
              value={`${dashboardData?.monthlyGrowth || 0}%`}
              icon={TrendingUp}
              description="Document processing growth"
              className="text-blue-600"
            />
            <StatsCard
              title="Processing Time"
              value={`${dashboardData?.avgProcessingTime || 0}h`}
              icon={Clock}
              description="Average processing time"
            />
            <StatsCard
              title="Efficiency"
              value={`${dashboardData?.efficiency || 0}%`}
              icon={CheckCircle}
              description="Overall system efficiency"
              className="text-green-600"
            />
          </div>
        </div>
      )}

      {/* Dashboard Widgets */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Pending Documents Widget */}
        <div className="bg-card rounded-lg border p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <FileText className="h-5 w-5" />
            Recent Pending Documents
          </h3>
          <div className="space-y-2">
            {dashboardData?.pendingDocumentsList?.slice(0, 5).map((doc) => (
              <div
                key={doc.id}
                className="flex justify-between items-center text-sm"
              >
                <span className="truncate">{doc.title}</span>
                <span className="text-muted-foreground">{doc.status}</span>
              </div>
            )) || (
              <p className="text-muted-foreground text-sm">
                No pending documents
              </p>
            )}
          </div>
        </div>

        {/* Assigned Cases Widget */}
        <div className="bg-card rounded-lg border p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <Briefcase className="h-5 w-5" />
            Recent Assigned Cases
          </h3>
          <div className="space-y-2">
            {dashboardData?.assignedCasesList?.slice(0, 5).map((caseItem) => (
              <div
                key={caseItem.id}
                className="flex justify-between items-center text-sm"
              >
                <span className="truncate">{caseItem.title}</span>
                <span className="text-muted-foreground">{caseItem.status}</span>
              </div>
            )) || (
              <p className="text-muted-foreground text-sm">No assigned cases</p>
            )}
          </div>
        </div>

        {/* System Status Widget */}
        <div className="bg-card rounded-lg border p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <MessageSquare className="h-5 w-5" />
            System Status
          </h3>
          <div className="space-y-2">
            <div className="flex justify-between items-center text-sm">
              <span>System Health</span>
              <span className="text-green-600">Operational</span>
            </div>
            <div className="flex justify-between items-center text-sm">
              <span>Last Backup</span>
              <span className="text-muted-foreground">Today</span>
            </div>
            <div className="flex justify-between items-center text-sm">
              <span>Active Users</span>
              <span className="text-muted-foreground">5</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
