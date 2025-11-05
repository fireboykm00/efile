import { useState } from "react";
import { DocumentUploadForm } from "@/components/documents/DocumentUploadForm";
import { useDocuments } from "@/hooks/useDocuments";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { DocumentStatus } from "@/types/document";
import { format } from "date-fns";
import { Download, Eye } from "lucide-react";

// Mock cases - replace with actual API call
const mockCases = [
  { id: "1", title: "Case 1: Smith vs. Jones" },
  { id: "2", title: "Case 2: Financial Audit 2024" },
];

const statusColors: Record<string, string> = {
  [DocumentStatus.PENDING]: "bg-yellow-100 text-yellow-800",
  [DocumentStatus.UNDER_REVIEW]: "bg-blue-100 text-blue-800",
  [DocumentStatus.APPROVED]: "bg-green-100 text-green-800",
  [DocumentStatus.REJECTED]: "bg-red-100 text-red-800",
  [DocumentStatus.ARCHIVED]: "bg-gray-100 text-gray-800",
};

export function DocumentsPage() {
  const { data, loading, error, refetch } = useDocuments();
  const [selectedStatus, setSelectedStatus] = useState<DocumentStatus | "ALL">(
    "ALL"
  );

  const documents = data?.documents || [];
  const filteredDocs =
    selectedStatus === "ALL"
      ? documents
      : documents.filter((doc) => doc.status === selectedStatus);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Documents</h1>
        <p className="text-muted-foreground">Manage and track all documents</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <DocumentUploadForm cases={mockCases} onSuccess={refetch} />
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
                      className="border rounded-lg p-4 flex justify-between items-start hover:bg-slate-50"
                    >
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
                      </div>
                      <div className="flex gap-2">
                        <Button variant="ghost" size="icon">
                          <Eye className="w-4 h-4" />
                        </Button>
                        <Button variant="ghost" size="icon">
                          <Download className="w-4 h-4" />
                        </Button>
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
