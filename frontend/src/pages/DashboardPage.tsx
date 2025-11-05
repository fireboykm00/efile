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
  Users,
  DollarSign,
  ShoppingCart,
  Calculator,
  Eye,
  Settings,
  AlertCircle,
} from "lucide-react";
import { UserRole } from "@/types/auth";

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

  const getRoleSpecificStats = () => {
    switch (userRole) {
      case UserRole.ADMIN:
        return (
          <>
            <StatsCard
              title="Total Users"
              value={dashboardData?.totalUsers || 0}
              icon={Users}
              description="All system users"
            />
            <StatsCard
              title="System Health"
              value="98%"
              icon={Settings}
              description="System operational"
              className="text-green-600"
            />
          </>
        );
      case UserRole.CEO:
        return (
          <>
            <StatsCard
              title="Company Growth"
              value={`${dashboardData?.monthlyGrowth || 0}%`}
              icon={TrendingUp}
              description="Monthly growth rate"
              className="text-blue-600"
            />
            <StatsCard
              title="Efficiency Rate"
              value={`${dashboardData?.efficiency || 0}%`}
              icon={CheckCircle}
              description="Overall efficiency"
              className="text-green-600"
            />
          </>
        );
      case UserRole.CFO:
        return (
          <>
            <StatsCard
              title="Financial Docs"
              value={dashboardData?.totalDocuments || 0}
              icon={DollarSign}
              description="Financial documents"
            />
            <StatsCard
              title="Processing Time"
              value={`${dashboardData?.avgProcessingTime || 0}h`}
              icon={Clock}
              description="Average processing"
            />
          </>
        );
      case UserRole.PROCUREMENT:
        return (
          <>
            <StatsCard
              title="Procurement Docs"
              value={dashboardData?.totalDocuments || 0}
              icon={ShoppingCart}
              description="Procurement documents"
            />
            <StatsCard
              title="Pending Bids"
              value={dashboardData?.pendingDocumentsCount || 0}
              icon={Clock}
              description="Awaiting approval"
              className="text-yellow-600"
            />
          </>
        );
      case UserRole.ACCOUNTANT:
        return (
          <>
            <StatsCard
              title="Accounting Docs"
              value={dashboardData?.totalDocuments || 0}
              icon={Calculator}
              description="Accounting documents"
            />
            <StatsCard
              title="Processed"
              value={dashboardData?.approvedDocuments || 0}
              icon={CheckCircle}
              description="Successfully processed"
              className="text-green-600"
            />
          </>
        );
      case UserRole.AUDITOR:
        return (
          <>
            <StatsCard
              title="Audit Scope"
              value={dashboardData?.totalDocuments || 0}
              icon={Eye}
              description="All documents in scope"
            />
            <StatsCard
              title="Compliance Rate"
              value={`${dashboardData?.efficiency || 0}%`}
              icon={CheckCircle}
              description="Compliance percentage"
              className="text-green-600"
            />
          </>
        );
      case UserRole.INVESTOR:
        return (
          <>
            <StatsCard
              title="Available Reports"
              value={dashboardData?.approvedDocuments || 0}
              icon={FileText}
              description="Approved documents"
            />
            <StatsCard
              title="Last Update"
              value="Today"
              icon={Clock}
              description="Latest report update"
            />
          </>
        );
      default:
        return null;
    }
  };

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

      {/* Basic Stats Grid */}
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

      {/* Role-specific Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {getRoleSpecificStats()}
      </div>

      {/* Cases and Messages for appropriate roles */}
      {(userRole === UserRole.ADMIN || 
        userRole === UserRole.CEO || 
        userRole === UserRole.CFO || 
        userRole === UserRole.AUDITOR) && (
        <div className="grid gap-4 md:grid-cols-2">
          <StatsCard
            title="Active Cases"
            value={dashboardData?.activeCases || 0}
            icon={Briefcase}
            description="Cases requiring attention"
          />
          <StatsCard
            title="Overdue Items"
            value={dashboardData?.overdueCasesCount || 0}
            icon={AlertCircle}
            description="Items overdue for review"
            className="text-red-600"
          />
        </div>
      )}

      {/* Dashboard Widgets */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Pending Documents Widget - Only for approvers */}
        {(userRole === UserRole.ADMIN || 
          userRole === UserRole.CEO || 
          userRole === UserRole.CFO) && (
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
        )}

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

        {/* System Status Widget - Admin only */}
        {userRole === UserRole.ADMIN && (
          <div className="bg-card rounded-lg border p-6">
            <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
              <Settings className="h-5 w-5" />
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
        )}

        {/* Messages Widget */}
        <div className="bg-card rounded-lg border p-6">
          <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
            <MessageSquare className="h-5 w-5" />
            Recent Messages
          </h3>
          <div className="space-y-2">
            {dashboardData?.unreadCommunicationsList?.slice(0, 5).map((msg) => (
              <div
                key={msg.id}
                className="flex justify-between items-center text-sm"
              >
                <span className="truncate">{msg.content}</span>
                <span className="text-muted-foreground">
                  {new Date(msg.sentAt).toLocaleDateString()}
                </span>
              </div>
            )) || (
              <p className="text-muted-foreground text-sm">No new messages</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
