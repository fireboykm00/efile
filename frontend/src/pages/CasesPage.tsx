import { useState } from "react";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { useCases, useCaseActions } from "@/hooks/useCases";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { CaseStatus } from "@/types/case";
import { format } from "date-fns";
import { Plus } from "lucide-react";

const caseSchema = z.object({
  title: z.string().min(5, "Title must be at least 5 characters"),
  description: z.string().min(20, "Description must be at least 20 characters"),
});

type CaseFormData = z.infer<typeof caseSchema>;

const statusColors: Record<string, string> = {
  [CaseStatus.OPEN]: "bg-red-100 text-red-800",
  [CaseStatus.IN_PROGRESS]: "bg-blue-100 text-blue-800",
  [CaseStatus.CLOSED]: "bg-green-100 text-green-800",
  [CaseStatus.ARCHIVED]: "bg-gray-100 text-gray-800",
};

export function CasesPage() {
  const [openDialog, setOpenDialog] = useState(false);
  const { data, loading, error, refetch } = useCases();
  const { createCase, loading: createLoading } = useCaseActions();

  const form = useForm<CaseFormData>({
    resolver: zodResolver(caseSchema),
  });

  const onSubmit = async (data: CaseFormData) => {
    const result = await createCase({
      title: data.title,
      description: data.description,
      status: CaseStatus.OPEN,
    });

    if (result) {
      form.reset();
      setOpenDialog(false);
      refetch();
    }
  };

  const cases = data?.cases || [];

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold">Cases</h1>
            <p className="text-muted-foreground">Manage and track all cases</p>
          </div>
          <Dialog open={openDialog} onOpenChange={setOpenDialog}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                New Case
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create New Case</DialogTitle>
              </DialogHeader>
              <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                  <FormField
                    control={form.control}
                    name="title"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Case Title</FormLabel>
                        <FormControl>
                          <Input placeholder="Enter case title" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="description"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Description</FormLabel>
                        <FormControl>
                          <Textarea
                            placeholder="Enter case description"
                            className="min-h-24"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <Button type="submit" disabled={createLoading} className="w-full">
                    {createLoading ? "Creating..." : "Create Case"}
                  </Button>
                </form>
              </Form>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>All Cases</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <p className="text-muted-foreground">Loading cases...</p>
            ) : error ? (
              <p className="text-red-600">Error: {error.message}</p>
            ) : cases.length === 0 ? (
              <p className="text-muted-foreground">No cases found</p>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {cases.map((caseItem) => (
                  <div
                    key={caseItem.id}
                    className="border rounded-lg p-4 hover:shadow-md transition"
                  >
                    <div className="flex justify-between items-start mb-2">
                      <h3 className="font-medium text-lg">{caseItem.title}</h3>
                      <Badge className={statusColors[caseItem.status]}>
                        {caseItem.status}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground line-clamp-2">
                      {caseItem.description}
                    </p>
                    <div className="flex justify-between items-center mt-4 text-xs text-muted-foreground">
                      <span>
                        Created {format(new Date(caseItem.createdAt), "MMM d, yyyy")}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
