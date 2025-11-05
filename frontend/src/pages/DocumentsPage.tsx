import { useState } from "react";
import { DocumentUploadForm } from "@/components/documents/DocumentUploadForm";
import { useDocuments } from "@/hooks/useDocuments";
import { useCases } from "@/hooks/useCases";
import { useUserRole } from "@/hooks/useAuthHooks";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { DocumentStatus } from "@/types/document";
import { UserRole } from "@/types/auth";
import { format } from "date-fns";
import { Download, Eye, CheckCircle, XCircle, Receipt, Send, Play, Archive } from "lucide-react";
import { toast } from "sonner";
import { documentService } from "@/services/documentService";

const statusColors: Record<string, string> = {
  [DocumentStatus.DRAFT]: "bg-gray-100 text-gray-800",
  [DocumentStatus.SUBMITTED]: "bg-yellow-100 text-yellow-800",
  [DocumentStatus.UNDER_REVIEW]: "bg-blue-100 text-blue-800",
  [DocumentStatus.APPROVED]: "bg-green-100 text-green-800",
  [DocumentStatus.REJECTED]: "bg-red-100 text-red-800",
  [DocumentStatus.WITHDRAWN]: "bg-gray-100 text-gray-800",
};

export function DocumentsPage() {
  const { data, loading, error, refetch, approveDocument, rejectDocument } = useDocuments();
  const { data: casesData } = useCases();
  const userRole = useUserRole();
  const [selectedStatus, setSelectedStatus] = useState<DocumentStatus | "ALL">(
    "ALL"
  );
  const [isRejecting, setIsRejecting] = useState<string | null>(null);

  const documents = data?.documents || [];
  const cases = casesData?.cases || [];
  
  // Transform cases for the form
  const caseOptions = cases.map(caseItem => ({
    id: caseItem.id,
    title: caseItem.title
  }));
  const filteredDocs =
    selectedStatus === "ALL"
      ? documents
      : documents.filter((doc) => doc.status === selectedStatus);

  const canApproveReject = userRole && [UserRole.ADMIN, UserRole.CEO, UserRole.CFO].includes(userRole);
  const canReview = userRole && [UserRole.ADMIN, UserRole.CEO, UserRole.CFO, UserRole.AUDITOR].includes(userRole);

  const handleApprove = async (documentId: string) => {
    try {
      await approveDocument(documentId);
      toast.success("Document approved successfully");
      refetch();
    } catch {
      toast.error("Failed to approve document");
    }
  };

  const handleReject = async (documentId: string) => {
    const reason = prompt("Please provide a reason for rejection:");
    if (!reason) return;

    try {
      setIsRejecting(documentId);
      await rejectDocument(documentId, reason);
      toast.success("Document rejected successfully");
      refetch();
    } catch {
      toast.error("Failed to reject document");
    } finally {
      setIsRejecting(null);
    }
  };

  const handleSubmit = async (documentId: string) => {
    try {
      await documentService.submitDocument(documentId);
      toast.success("Document submitted for review");
      refetch();
    } catch {
      toast.error("Failed to submit document");
    }
  };

  const handleStartReview = async (documentId: string) => {
    try {
      await documentService.startReview(documentId);
      toast.success("Document review started");
      refetch();
    } catch {
      toast.error("Failed to start review");
    }
  };

  const handleWithdraw = async (documentId: string) => {
    if (!confirm("Are you sure you want to withdraw this document?")) return;
    
    try {
      await documentService.withdrawDocument(documentId);
      toast.success("Document withdrawn");
      refetch();
    } catch {
      toast.error("Failed to withdraw document");
    }
  };

  const handleDownloadReceipt = async (documentId: string) => {
    try {
      const response = await fetch(`/api/documents/${documentId}/receipt`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to download receipt');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = `receipt_${documentId}.txt`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      
      toast.success("Receipt downloaded successfully");
    } catch {
      toast.error("Failed to download receipt");
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Documents</h1>
        <p className="text-muted-foreground">Manage and track all documents</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <DocumentUploadForm cases={caseOptions} onSuccess={refetch} />
        </div>

        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>Documents List</CardTitle>
              <div className="flex gap-2 mt-4">
                {["ALL", ...Object.values(DocumentStatus)].map((status) => (
                  <Button
                    key={status}
                    variant={selectedStatus === status ? "default" : "outline"}
                    size="sm"
                    onClick={() =>
                      setSelectedStatus(status as DocumentStatus | "ALL")
                    }
                  >
                    {status}
                  </Button>
                ))}
              </div>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="text-muted-foreground">Loading documents...</p>
              ) : error ? (
                <p className="text-red-600">Error: {error.message}</p>
              ) : filteredDocs.length === 0 ? (
                <p className="text-muted-foreground">No documents found</p>
              ) : (
                <div className="space-y-3">
                  {filteredDocs.map((doc) => (
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
                          {doc.approvedBy && (
                            <p className="text-xs text-muted-foreground">
                              Approved by: {doc.approvedByName}
                            </p>
                          )}
                        </div>
                        <div className="flex gap-2">
                          <Button variant="ghost" size="icon">
                            <Eye className="w-4 h-4" />
                          </Button>
                          <Button variant="ghost" size="icon">
                            <Download className="w-4 h-4" />
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleDownloadReceipt(doc.id)}
                            title="Download receipt"
                          >
                            <Receipt className="w-4 h-4" />
                          </Button>
                          
                          {/* Status-based actions */}
                          {doc.status === DocumentStatus.DRAFT && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleSubmit(doc.id)}
                              className="text-blue-600 hover:text-blue-700"
                              title="Submit document for review"
                            >
                              <Send className="w-4 h-4" />
                            </Button>
                          )}
                          
                          {canReview && doc.status === DocumentStatus.SUBMITTED && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleStartReview(doc.id)}
                              className="text-purple-600 hover:text-purple-700"
                              title="Start review"
                            >
                              <Play className="w-4 h-4" />
                            </Button>
                          )}
                          
                          {canApproveReject && doc.status === DocumentStatus.UNDER_REVIEW && (
                            <>
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleApprove(doc.id)}
                                className="text-green-600 hover:text-green-700"
                                title="Approve document"
                              >
                                <CheckCircle className="w-4 h-4" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="icon"
                                onClick={() => handleReject(doc.id)}
                                className="text-red-600 hover:text-red-700"
                                title="Reject document"
                                disabled={isRejecting === doc.id}
                              >
                                <XCircle className="w-4 h-4" />
                              </Button>
                            </>
                          )}
                          
                          {doc.status === DocumentStatus.REJECTED && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleSubmit(doc.id)}
                              className="text-blue-600 hover:text-blue-700"
                              title="Resubmit document"
                            >
                              <Send className="w-4 h-4" />
                            </Button>
                          )}
                          
                          {(doc.status === DocumentStatus.DRAFT || doc.status === DocumentStatus.SUBMITTED || doc.status === DocumentStatus.REJECTED) && (
                            <Button
                              variant="ghost"
                              size="icon"
                              onClick={() => handleWithdraw(doc.id)}
                              className="text-gray-600 hover:text-gray-700"
                              title="Withdraw document"
                            >
                              <Archive className="w-4 h-4" />
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
