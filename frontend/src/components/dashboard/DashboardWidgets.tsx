import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { Document, DocumentStatus } from "@/types/document";
import { Case, CaseStatus } from "@/types/case";
import { Communication } from "@/types/communication";
import { DashboardSummary } from "@/services/dashboardService";
import { FileText, Briefcase, MessageSquare, AlertCircle } from "lucide-react";
import { format } from "date-fns";

const statusColors: Record<string, string> = {
  [DocumentStatus.PENDING]: "bg-yellow-100 text-yellow-800",
  [DocumentStatus.UNDER_REVIEW]: "bg-blue-100 text-blue-800",
  [DocumentStatus.APPROVED]: "bg-green-100 text-green-800",
  [DocumentStatus.REJECTED]: "bg-red-100 text-red-800",
  [DocumentStatus.ARCHIVED]: "bg-gray-100 text-gray-800",
  [CaseStatus.OPEN]: "bg-red-100 text-red-800",
  [CaseStatus.IN_PROGRESS]: "bg-blue-100 text-blue-800",
  [CaseStatus.CLOSED]: "bg-green-100 text-green-800",
};

interface WidgetProps {
  loading: boolean;
  error?: Error | null;
}

export function PendingDocumentsWidget({
  documents,
  loading,
  error,
}: WidgetProps & { documents: Document[] }) {
  if (loading) return <WidgetSkeleton />;
  if (error) return <WidgetError error={error} />;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <FileText className="w-5 h-5" />
          Pending Documents
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {documents.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No pending documents
            </p>
          ) : (
            documents.map((doc) => (
              <div key={doc.id} className="flex justify-between items-start">
                <div className="flex-1">
                  <p className="font-medium text-sm">{doc.title}</p>
                  <p className="text-xs text-muted-foreground">
                    {format(new Date(doc.createdAt!), "MMM d, yyyy")}
                  </p>
                </div>
                <Badge className={statusColors[doc.status]}>{doc.status}</Badge>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

export function AssignedCasesWidget({
  cases,
  loading,
  error,
}: WidgetProps & { cases: Case[] }) {
  if (loading) return <WidgetSkeleton />;
  if (error) return <WidgetError error={error} />;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Briefcase className="w-5 h-5" />
          Assigned Cases
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {cases.length === 0 ? (
            <p className="text-sm text-muted-foreground">No assigned cases</p>
          ) : (
            cases.map((caseItem) => (
              <div
                key={caseItem.id}
                className="flex justify-between items-start"
              >
                <div className="flex-1">
                  <p className="font-medium text-sm">{caseItem.title}</p>
                  <p className="text-xs text-muted-foreground">
                    {format(new Date(caseItem.createdAt), "MMM d, yyyy")}
                  </p>
                </div>
                <Badge className={statusColors[caseItem.status]}>
                  {caseItem.status}
                </Badge>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

export function NotificationsWidget({
  communications,
  loading,
  error,
}: WidgetProps & { communications: Communication[] }) {
  if (loading) return <WidgetSkeleton />;
  if (error) return <WidgetError error={error} />;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MessageSquare className="w-5 h-5" />
          Recent Notifications
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {communications.length === 0 ? (
            <p className="text-sm text-muted-foreground">
              No unread notifications
            </p>
          ) : (
            communications.map((comm) => (
              <div key={comm.id} className="flex gap-3 p-2 bg-slate-50 rounded">
                <AlertCircle className="w-4 h-4 shrink-0 mt-0.5 text-primary" />
                <div className="flex-1">
                  <p className="text-sm">{comm.content}</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    {format(new Date(comm.sentAt), "MMM d, yyyy HH:mm")}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

interface SummaryCardsProps {
  summary: DashboardSummary;
}

export function SummaryCards({ summary }: SummaryCardsProps) {
  const summaryData = [
    {
      title: "Pending Documents",
      value: summary?.pendingDocumentsCount || 0,
      icon: <FileText className="w-5 h-5" />,
    },
    {
      title: "Assigned Cases",
      value: summary?.assignedCasesCount || 0,
      icon: <Briefcase className="w-5 h-5" />,
    },
    {
      title: "Unread Messages",
      value: summary?.unreadCommunicationsCount || 0,
      icon: <MessageSquare className="w-5 h-5" />,
    },
    {
      title: "Overdue Items",
      value: summary?.overdueCasesCount || 0,
      icon: <AlertCircle className="w-5 h-5" />,
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      {summaryData.map((item, index) => (
        <Card key={index}>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">{item.title}</p>
                <p className="text-3xl font-bold mt-2">{item.value}</p>
              </div>
              <div className="text-primary opacity-50">{item.icon}</div>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function WidgetSkeleton() {
  return (
    <Card>
      <CardHeader>
        <Skeleton className="h-6 w-40" />
      </CardHeader>
      <CardContent className="space-y-3">
        {[1, 2, 3].map((i) => (
          <div key={i} className="space-y-2">
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-3 w-24" />
          </div>
        ))}
      </CardContent>
    </Card>
  );
}

function WidgetError({ error }: { error: Error }) {
  return (
    <Card className="border-red-200">
      <CardContent className="pt-6">
        <p className="text-sm text-red-600">
          Error loading data: {error.message}
        </p>
      </CardContent>
    </Card>
  );
}
