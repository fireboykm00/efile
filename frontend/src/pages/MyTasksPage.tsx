import { useState, useEffect, useCallback } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { caseService } from "@/services/caseService";
import { Case, CaseStatus, CasePriority } from "@/types/case";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { format } from "date-fns";
import { 
  Briefcase, 
  Clock, 
  Calendar,
  ArrowRight
} from "lucide-react";

const statusColors: Record<string, string> = {
  [CaseStatus.OPEN]: "bg-red-100 text-red-800",
  [CaseStatus.ACTIVE]: "bg-blue-100 text-blue-800",
  [CaseStatus.UNDER_REVIEW]: "bg-purple-100 text-purple-800",
  [CaseStatus.COMPLETED]: "bg-green-100 text-green-800",
  [CaseStatus.ON_HOLD]: "bg-yellow-100 text-yellow-800",
  [CaseStatus.CLOSED]: "bg-gray-100 text-gray-800",
};

const priorityColors: Record<string, string> = {
  [CasePriority.LOW]: "bg-gray-100 text-gray-800",
  [CasePriority.MEDIUM]: "bg-blue-100 text-blue-800",
  [CasePriority.HIGH]: "bg-orange-100 text-orange-800",
  [CasePriority.URGENT]: "bg-red-100 text-red-800",
};

export function MyTasksPage() {
  const { user } = useAuth();
  const [cases, setCases] = useState<Case[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMyCases = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await caseService.getCases();
      // Filter cases assigned to current user
      const myCases = data.cases.filter(caseItem => 
        caseItem.assignedTo?.id === user?.id
      );
      setCases(myCases);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch cases");
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    fetchMyCases();
  }, [fetchMyCases]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">Loading your assigned cases...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-red-600">Error loading cases: {error}</p>
          <Button onClick={fetchMyCases} className="mt-4">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Assigned Cases</h1>
          <p className="text-muted-foreground">Cases assigned to you for action</p>
        </div>
        <Button onClick={() => window.location.href = '/cases'}>
          View All Cases
          <ArrowRight className="ml-2 h-4 w-4" />
        </Button>
      </div>

      {/* Cases List */}
      <Card>
        <CardHeader>
          <CardTitle>Your Assigned Cases ({cases.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {cases.length === 0 ? (
            <div className="text-center py-8">
              <Briefcase className="mx-auto h-12 w-12 text-gray-400" />
              <h3 className="mt-2 text-sm font-semibold text-gray-900">No cases assigned</h3>
              <p className="mt-1 text-sm text-gray-500">
                No cases have been assigned to you yet
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {cases.map((caseItem) => (
                <div
                  key={caseItem.id}
                  className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="font-medium">{caseItem.title}</h3>
                      <Badge className={statusColors[caseItem.status]}>
                        {caseItem.status}
                      </Badge>
                      <Badge className={priorityColors[caseItem.priority]}>
                        {caseItem.priority}
                      </Badge>
                    </div>
                    <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                      {caseItem.description}
                    </p>
                    <div className="flex items-center gap-4 text-xs text-gray-500">
                      <div className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        Created: {format(new Date(caseItem.createdAt), 'MMM d, yyyy')}
                      </div>
                      {caseItem.dueDate && (
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          Due: {format(new Date(caseItem.dueDate), 'MMM d, yyyy')}
                        </div>
                      )}
                      {caseItem.department && (
                        <div>Department: {caseItem.department}</div>
                      )}
                    </div>
                  </div>
                  <div className="ml-4">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => window.location.href = `/cases/${caseItem.id}`}
                    >
                      Take Action
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
