import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Case, CaseStatus } from "@/types/case";
import { format } from "date-fns";
import { ArrowLeft, FileText, User, Calendar, Edit, CheckCircle, Clock, Pause, Square, Upload } from "lucide-react";
import { toast } from "sonner";
import { caseService } from "@/services/caseService";
import { documentService } from "@/services/documentService";
import { DocumentUploadForm } from "@/components/documents/DocumentUploadForm";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";

const statusColors: Record<string, string> = {
  [CaseStatus.OPEN]: "bg-red-100 text-red-800",
  [CaseStatus.ACTIVE]: "bg-blue-100 text-blue-800",
  [CaseStatus.UNDER_REVIEW]: "bg-purple-100 text-purple-800",
  [CaseStatus.COMPLETED]: "bg-green-100 text-green-800",
  [CaseStatus.ON_HOLD]: "bg-orange-100 text-orange-800",
  [CaseStatus.CLOSED]: "bg-gray-100 text-gray-800",
};

const statusIcons: Record<string, React.ReactNode> = {
  [CaseStatus.OPEN]: <Clock className="w-4 h-4" />,
  [CaseStatus.ACTIVE]: <CheckCircle className="w-4 h-4" />,
  [CaseStatus.UNDER_REVIEW]: <FileText className="w-4 h-4" />,
  [CaseStatus.COMPLETED]: <CheckCircle className="w-4 h-4" />,
  [CaseStatus.ON_HOLD]: <Pause className="w-4 h-4" />,
  [CaseStatus.CLOSED]: <Square className="w-4 h-4" />,
};

export function CaseDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [caseData, setCaseData] = useState<Case | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCaseDetails = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await caseService.getCaseWithDocuments(id);
      setCaseData(data);
    } catch (err) {
      setError("Failed to load case details");
      console.error("Error loading case:", err);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    fetchCaseDetails();
  }, [fetchCaseDetails]);

  const handleStatusUpdate = async (newStatus: CaseStatus) => {
    if (!id || !caseData) return;

    try {
      const updatedCase = await caseService.updateCaseStatus(id, newStatus);
      setCaseData(updatedCase);
      toast.success(`Case status updated to ${newStatus}`);
    } catch (err) {
      toast.error("Failed to update case status");
      console.error("Error updating case status:", err);
    }
  };

  const handleDownloadDocument = async (documentId: string) => {
    try {
      const blob = await documentService.downloadDocument(documentId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `document-${documentId}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err) {
      toast.error("Failed to download document");
      console.error("Error downloading document:", err);
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center">Loading case details...</div>
      </div>
    );
  }

  if (error || !caseData) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="text-center text-red-600">{error || "Case not found"}</div>
      </div>
    );
  }

  const canEditCase = true; // TODO: Add role-based logic
  const canUpdateStatus = true; // TODO: Add role-based logic

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <Button
          variant="ghost"
          onClick={() => navigate("/cases")}
          className="mb-4"
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Cases
        </Button>
        
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold">{caseData.title}</h1>
            <p className="text-muted-foreground mt-2">{caseData.description}</p>
          </div>
          
          <div className="flex gap-2">
            {canEditCase && (
              <Button variant="outline" onClick={() => navigate(`/cases/${id}/edit`)}>
                <Edit className="w-4 h-4 mr-2" />
                Edit Case
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Case Info */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Case Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Status</label>
              <div className="flex items-center gap-2 mt-1">
                <Badge className={statusColors[caseData.status]}>
                  {statusIcons[caseData.status]}
                  <span className="ml-1">{caseData.status}</span>
                </Badge>
              </div>
            </div>
            
            <div>
              <label className="text-sm font-medium text-muted-foreground">Created</label>
              <div className="flex items-center gap-2 mt-1">
                <Calendar className="w-4 h-4" />
                <span>{format(caseData.createdAt, "MMM d, yyyy HH:mm")}</span>
              </div>
            </div>
            
            <div>
              <label className="text-sm font-medium text-muted-foreground">Last Updated</label>
              <div className="flex items-center gap-2 mt-1">
                <Calendar className="w-4 h-4" />
                <span>{format(caseData.updatedAt, "MMM d, yyyy HH:mm")}</span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="w-5 h-5" />
              People
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">Created By</label>
              <div className="mt-1">
                <p>{caseData.createdBy?.name}</p>
                <p className="text-sm text-muted-foreground">{caseData.createdBy?.email}</p>
              </div>
            </div>
            
            {caseData.assignedTo && (
              <div>
                <label className="text-sm font-medium text-muted-foreground">Assigned To</label>
                <div className="mt-1">
                  <p>{caseData.assignedTo.name}</p>
                  <p className="text-sm text-muted-foreground">{caseData.assignedTo.email}</p>
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {canUpdateStatus && caseData.status !== CaseStatus.CLOSED && (
              <>
                {caseData.status === CaseStatus.OPEN && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.ACTIVE)} className="w-full">
                    Set to Active
                  </Button>
                )}
                {caseData.status === CaseStatus.ACTIVE && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.UNDER_REVIEW)} className="w-full">
                    Start Review
                  </Button>
                )}
                {caseData.status === CaseStatus.UNDER_REVIEW && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.COMPLETED)} className="w-full">
                    Mark Complete
                  </Button>
                )}
                {(caseData.status === CaseStatus.ACTIVE || caseData.status === CaseStatus.UNDER_REVIEW) && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.ON_HOLD)} variant="outline" className="w-full">
                    Put on Hold
                  </Button>
                )}
                {caseData.status === CaseStatus.ON_HOLD && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.ACTIVE)} className="w-full">
                    Resume
                  </Button>
                )}
                {(caseData.status === CaseStatus.COMPLETED || caseData.status === CaseStatus.ON_HOLD) && (
                  <Button onClick={() => handleStatusUpdate(CaseStatus.CLOSED)} variant="outline" className="w-full">
                    Close Case
                  </Button>
                )}
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Documents */}
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5" />
              Documents ({caseData.documents?.length || 0})
            </CardTitle>
            <Dialog>
              <DialogTrigger asChild>
                <Button size="sm">
                  <Upload className="w-4 h-4 mr-2" />
                  Add Document
                </Button>
              </DialogTrigger>
              <DialogContent className="max-w-2xl">
                <DialogHeader>
                  <DialogTitle>Upload Document to Case</DialogTitle>
                </DialogHeader>
                <DocumentUploadForm 
                  cases={[{ id: caseData.id, title: caseData.title }]} 
                  onSuccess={() => {
                    fetchCaseDetails();
                    toast.success("Document uploaded successfully");
                  }}
                  
                />
              </DialogContent>
            </Dialog>
          </div>
        </CardHeader>
        <CardContent>
          {caseData.documents && caseData.documents.length > 0 ? (
            <div className="space-y-3">
              {caseData.documents.map((doc) => (
                <div
                  key={doc.id}
                  className="border rounded-lg p-4 hover:bg-slate-50"
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <p className="font-medium">{doc.title}</p>
                      <div className="flex gap-2 mt-2">
                        <Badge variant="outline">{doc.type}</Badge>
                        <Badge className={statusColors[doc.status]}>
                          {doc.status}
                        </Badge>
                      </div>
                      <p className="text-xs text-muted-foreground mt-2">
                        {format(doc.uploadedAt, "MMM d, yyyy HH:mm")} •{" "}
                        {doc.receiptNumber && `Receipt: ${doc.receiptNumber}`}
                      </p>
                      {doc.uploadedByName && (
                        <p className="text-xs text-muted-foreground">
                          Uploaded by: {doc.uploadedByName}
                        </p>
                      )}
                    </div>
                    <div className="flex gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDownloadDocument(doc.id)}
                      >
                        Download
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-muted-foreground text-center py-8">
              No documents attached to this case
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
