import { useState, useEffect, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { caseService } from "@/services/caseService";
import { CaseStatus, CasePriority, CaseCategory, UpdateCaseRequest } from "@/types/case";
import { User, Department } from "@/types/user";
import { useUsers } from "@/hooks/useUsers";
import { useDepartments } from "@/hooks/useDepartments";
import { ArrowLeft, Save } from "lucide-react";
import { toast } from "sonner";

const caseEditSchema = z.object({
  title: z.string().min(5, "Title must be at least 5 characters"),
  description: z.string().min(20, "Description must be at least 20 characters"),
  status: z.nativeEnum(CaseStatus),
  priority: z.nativeEnum(CasePriority),
  category: z.nativeEnum(CaseCategory),
  assignedToId: z.string().optional(),
  departmentId: z.string().optional(),
  dueDate: z.string().optional(),
});

type CaseEditFormData = z.infer<typeof caseEditSchema>;

export function CaseEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const { data: users } = useUsers();
  const { data: departments } = useDepartments();

  const form = useForm<CaseEditFormData>({
    resolver: zodResolver(caseEditSchema),
    defaultValues: {
      title: "",
      description: "",
      status: CaseStatus.OPEN,
      priority: CasePriority.MEDIUM,
      category: CaseCategory.GENERAL,
      assignedToId: "unassigned",
      departmentId: "none",
      dueDate: "",
    },
  });

  const fetchCaseDetails = useCallback(async () => {
    if (!id) return;

    try {
      setLoading(true);
      const data = await caseService.getCaseWithDocuments(id);
      
      // Set form values
      form.reset({
        title: data.title,
        description: data.description,
        status: data.status,
        priority: data.priority,
        category: data.category,
        assignedToId: data.assignedTo?.id || "unassigned",
        departmentId: data.department || "none",
        dueDate: data.dueDate ? new Date(data.dueDate).toISOString().split('T')[0] : "",
      });
    } catch {
      toast.error("Failed to load case details");
      navigate(`/cases/${id}`);
    } finally {
      setLoading(false);
    }
  }, [id, form, navigate]);

  useEffect(() => {
    fetchCaseDetails();
  }, [fetchCaseDetails]);

  const onSubmit = async (data: CaseEditFormData) => {
    if (!id) return;

    try {
      setSaving(true);
      
      const updateData: UpdateCaseRequest = {
        title: data.title,
        description: data.description,
        status: data.status,
        priority: data.priority,
        category: data.category,
        dueDate: data.dueDate,
        ...(data.assignedToId && data.assignedToId !== "unassigned" && { assignedToId: data.assignedToId }),
        ...(data.departmentId && data.departmentId !== "none" && { departmentId: data.departmentId }),
      };

      await caseService.updateCase(id, updateData);
      toast.success("Case updated successfully");
      navigate(`/cases/${id}`);
    } catch {
      toast.error("Failed to update case");
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
          <p className="mt-4 text-muted-foreground">Loading case details...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button
          variant="outline"
          onClick={() => navigate(`/cases/${id}`)}
        >
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back to Case
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Edit Case</h1>
          <p className="text-muted-foreground">Update case information</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Case Details</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <FormField
                  control={form.control}
                  name="title"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Title</FormLabel>
                      <FormControl>
                        <Input placeholder="Enter case title" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="status"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Status</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Select status" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={CaseStatus.OPEN}>Open</SelectItem>
                          <SelectItem value={CaseStatus.ACTIVE}>Active</SelectItem>
                          <SelectItem value={CaseStatus.UNDER_REVIEW}>Under Review</SelectItem>
                          <SelectItem value={CaseStatus.COMPLETED}>Completed</SelectItem>
                          <SelectItem value={CaseStatus.ON_HOLD}>On Hold</SelectItem>
                          <SelectItem value={CaseStatus.CLOSED}>Closed</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="priority"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Priority</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Select priority" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={CasePriority.LOW}>Low</SelectItem>
                          <SelectItem value={CasePriority.MEDIUM}>Medium</SelectItem>
                          <SelectItem value={CasePriority.HIGH}>High</SelectItem>
                          <SelectItem value={CasePriority.URGENT}>Urgent</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="category"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Category</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Select category" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value={CaseCategory.GENERAL}>General</SelectItem>
                          <SelectItem value={CaseCategory.LEGAL}>Legal</SelectItem>
                          <SelectItem value={CaseCategory.FINANCIAL}>Financial</SelectItem>
                          <SelectItem value={CaseCategory.HR}>HR</SelectItem>
                          <SelectItem value={CaseCategory.COMPLIANCE}>Compliance</SelectItem>
                          <SelectItem value={CaseCategory.OPERATIONS}>Operations</SelectItem>
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="assignedToId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Assigned To</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Assign to user" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="unassigned">Unassigned</SelectItem>
                          {users?.map((user: User) => (
                            <SelectItem key={user.id} value={user.id}>
                              {user.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="departmentId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Department</FormLabel>
                      <Select onValueChange={field.onChange} defaultValue={field.value}>
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Select department" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          <SelectItem value="none">No Department</SelectItem>
                          {departments?.map((department: Department) => (
                            <SelectItem key={department.id} value={department.id}>
                              {department.name}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="dueDate"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Due Date</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description</FormLabel>
                    <FormControl>
                      <Textarea
                        placeholder="Enter case description"
                        className="min-h-[120px]"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="flex gap-4">
                <Button type="submit" disabled={saving}>
                  <Save className="w-4 h-4 mr-2" />
                  {saving ? "Saving..." : "Save Changes"}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate(`/cases/${id}`)}
                >
                  Cancel
                </Button>
              </div>
            </form>
          </Form>
        </CardContent>
      </Card>
    </div>
  );
}
