import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Download, FileText } from "lucide-react";

export function ReportsPage() {
  const [selectedFormat, setSelectedFormat] = useState("csv");

  const handleExport = (type: string) => {
    console.log(`Exporting ${type} as ${selectedFormat}`);
    // TODO: Implement export functionality
  };

  const reports = [
    {
      id: 1,
      name: "Documents Report",
      description: "Export all documents with their status and metadata",
      icon: FileText,
    },
    {
      id: 2,
      name: "Cases Report",
      description: "Export all cases with assignments and status",
      icon: FileText,
    },
    {
      id: 3,
      name: "Users Report",
      description: "Export all users with their roles and departments",
      icon: FileText,
    },
    {
      id: 4,
      name: "Communications Report",
      description: "Export all communications and messages",
      icon: FileText,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Reports</h1>
        <p className="text-muted-foreground">
          Generate and export reports in various formats
        </p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Export Format</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-4 items-center">
            <Select value={selectedFormat} onValueChange={setSelectedFormat}>
              <SelectTrigger className="w-48">
                <SelectValue placeholder="Select format" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="csv">CSV</SelectItem>
                <SelectItem value="pdf">PDF</SelectItem>
                <SelectItem value="xlsx">Excel</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-sm text-muted-foreground">
              Choose the format for your export
            </p>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {reports.map((report) => {
          const Icon = report.icon;
          return (
            <Card key={report.id} className="hover:shadow-md transition">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <Icon className="h-6 w-6 text-primary" />
                    <div>
                      <CardTitle className="text-lg">{report.name}</CardTitle>
                      <p className="text-sm text-muted-foreground">
                        {report.description}
                      </p>
                    </div>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <Button
                  onClick={() => handleExport(report.name)}
                  className="w-full"
                  variant="outline"
                >
                  <Download className="mr-2 h-4 w-4" />
                  Export as {selectedFormat.toUpperCase()}
                </Button>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}

