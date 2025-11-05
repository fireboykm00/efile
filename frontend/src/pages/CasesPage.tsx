import { useState } from "react";
import { useNavigate } from "react-router-dom";
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
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Resolver, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { 
  CaseStatus, 
  CasePriority, 
  CaseCategory
} from "@/types/case";
import { format } from "date-fns";
import { 
  Plus, 
  Calendar, 
  DollarSign, 
  MapPin, 
  Tag, 
  Users,
  FileText
} from "lucide-react";
import { toast } from "sonner";

// Enhanced case schema with comprehensive fields
const caseSchema = z.object({
  title: z.string().min(5, "Title must be at least 5 characters"),
  description: z.string().min(20, "Description must be at least 20 characters"),
  priority: z.string().min(1, "Priority is required"),
  category: z.string().min(1, "Category is required"),
  tags: z.array(z.string()),
  assignedToId: z.string().optional(),
  dueDate: z.string().optional(),
  estimatedCompletionDate: z.string().optional(),
  budget: z.number().positive().optional(),
  location: z.string().optional(),
  department: z.string().optional(),
  stakeholderIds: z.array(z.string()),
  attachments: z.array(z.any()),
});

type CaseFormData = z.infer<typeof caseSchema>;

const statusColors: Record<string, string> = {
  [CaseStatus.OPEN]: "bg-red-100 text-red-800",
  [CaseStatus.ACTIVE]: "bg-blue-100 text-blue-800",
  [CaseStatus.UNDER_REVIEW]: "bg-purple-100 text-purple-800",
  [CaseStatus.COMPLETED]: "bg-green-100 text-green-800",
  [CaseStatus.ON_HOLD]: "bg-orange-100 text-orange-800",
  [CaseStatus.CLOSED]: "bg-gray-100 text-gray-800",
};

const priorityColors: Record<string, string> = {
  [CasePriority.LOW]: "bg-gray-100 text-gray-800",
  [CasePriority.MEDIUM]: "bg-blue-100 text-blue-800",
  [CasePriority.HIGH]: "bg-orange-100 text-orange-800",
  [CasePriority.URGENT]: "bg-red-100 text-red-800",
};

const categoryColors: Record<string, string> = {
  [CaseCategory.GENERAL]: "bg-gray-100 text-gray-800",
  [CaseCategory.LEGAL]: "bg-purple-100 text-purple-800",
  [CaseCategory.FINANCIAL]: "bg-green-100 text-green-800",
  [CaseCategory.HR]: "bg-blue-100 text-blue-800",
  [CaseCategory.COMPLIANCE]: "bg-red-100 text-red-800",
  [CaseCategory.OPERATIONS]: "bg-orange-100 text-orange-800",
  [CaseCategory.STRATEGIC]: "bg-indigo-100 text-indigo-800",
};

export function CasesPage() {
  const navigate = useNavigate();
  const [openDialog, setOpenDialog] = useState(false);
  const { data, loading, error, refetch } = useCases();
  const { createCase, loading: createLoading } = useCaseActions();

  const form = useForm<CaseFormData>({
    resolver: zodResolver(caseSchema) as Resolver<CaseFormData>,
    defaultValues: {
      title: "",
      description: "",
      priority: CasePriority.MEDIUM,
      category: CaseCategory.GENERAL,
      tags: [],
      assignedToId: "unassigned",
      dueDate: "",
      estimatedCompletionDate: "",
      budget: undefined,
      location: "",
      department: "",
      stakeholderIds: [],
      attachments: [],
    },
  });

  const onSubmit = async (data: CaseFormData) => {
    try {
      const result = await createCase({
        title: data.title,
        description: data.description,
        status: CaseStatus.OPEN,
        priority: data.priority as CasePriority,
        category: data.category as CaseCategory,
        tags: data.tags,
        assignedToId: data.assignedToId,
        dueDate: data.dueDate,
        estimatedCompletionDate: data.estimatedCompletionDate,
        budget: data.budget,
        location: data.location,
        department: data.department,
        stakeholderIds: data.stakeholderIds,
        attachments: data.attachments,
      });

      if (result) {
        form.reset();
        setOpenDialog(false);
        refetch();
        toast.success("Case created successfully");
      }
    } catch (error) {
      toast.error("Failed to create case");
      console.error("Error creating case:", error);
    }
  };

  const cases = data?.cases || [];

  return (
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
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <Plus className="h-5 w-5" />
                Create New Case
              </DialogTitle>
            </DialogHeader>
            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="space-y-6"
              >
                {/* Basic Information */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    Basic Information
                  </h3>
                  
                  <FormField
                    control={form.control}
                    name="title"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Case Title *</FormLabel>
                        <FormControl>
                          <Input placeholder="Enter descriptive case title" {...field} />
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
                        <FormLabel>Description *</FormLabel>
                        <FormControl>
                          <Textarea
                            placeholder="Provide detailed case description"
                            className="min-h-32"
                            {...field}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                {/* Classification */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <Tag className="h-4 w-4" />
                    Classification
                  </h3>
                  
                  <div className="grid grid-cols-2 gap-4">
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
                              <SelectItem value={CasePriority.LOW}>
                                <div className="flex items-center gap-2">
                                  <div className="w-2 h-2 bg-gray-500 rounded-full"></div>
                                  Low Priority
                                </div>
                              </SelectItem>
                              <SelectItem value={CasePriority.MEDIUM}>
                                <div className="flex items-center gap-2">
                                  <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                                  Medium Priority
                                </div>
                              </SelectItem>
                              <SelectItem value={CasePriority.HIGH}>
                                <div className="flex items-center gap-2">
                                  <div className="w-2 h-2 bg-orange-500 rounded-full"></div>
                                  High Priority
                                </div>
                              </SelectItem>
                              <SelectItem value={CasePriority.URGENT}>
                                <div className="flex items-center gap-2">
                                  <div className="w-2 h-2 bg-red-500 rounded-full"></div>
                                  Urgent Priority
                                </div>
                              </SelectItem>
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
                              <SelectItem value={CaseCategory.HR}>Human Resources</SelectItem>
                              <SelectItem value={CaseCategory.COMPLIANCE}>Compliance</SelectItem>
                              <SelectItem value={CaseCategory.OPERATIONS}>Operations</SelectItem>
                              <SelectItem value={CaseCategory.STRATEGIC}>Strategic</SelectItem>
                            </SelectContent>
                          </Select>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>

                  <FormField
                    control={form.control}
                    name="tags"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Tags</FormLabel>
                        <FormControl>
                          <Input 
                            placeholder="Enter tags separated by commas" 
                            {...field}
                            onChange={(e) => field.onChange(e.target.value.split(',').map(tag => tag.trim()).filter(Boolean))}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                {/* Assignment & People */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <Users className="h-4 w-4" />
                    Assignment & People
                  </h3>
                  
                  <FormField
                    control={form.control}
                    name="assignedToId"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Assigned To</FormLabel>
                        <Select onValueChange={field.onChange} defaultValue={field.value}>
                          <FormControl>
                            <SelectTrigger>
                              <SelectValue placeholder="Select assignee" />
                            </SelectTrigger>
                          </FormControl>
                          <SelectContent>
                            <SelectItem value="unassigned">Unassigned</SelectItem>
                            {/* TODO: Load users from API */}
                            <SelectItem value="user1">John Doe</SelectItem>
                            <SelectItem value="user2">Jane Smith</SelectItem>
                          </SelectContent>
                        </Select>
                        <FormMessage />
                      </FormItem>
                    )}
                  />

                  <FormField
                    control={form.control}
                    name="department"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Department</FormLabel>
                        <FormControl>
                          <Input placeholder="Enter department name" {...field} />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                {/* Timeline & Dates */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    Timeline & Dates
                  </h3>
                  
                  <div className="grid grid-cols-2 gap-4">
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

                    <FormField
                      control={form.control}
                      name="estimatedCompletionDate"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Est. Completion</FormLabel>
                          <FormControl>
                            <Input type="date" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>
                </div>

                {/* Financial & Location */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <DollarSign className="h-4 w-4" />
                    Financial & Location
                  </h3>
                  
                  <div className="grid grid-cols-2 gap-4">
                    <FormField
                      control={form.control}
                      name="budget"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Budget ($)</FormLabel>
                          <FormControl>
                            <Input 
                              type="number" 
                              placeholder="0.00"
                              {...field}
                              onChange={(e) => field.onChange(parseFloat(e.target.value) || undefined)}
                            />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />

                    <FormField
                      control={form.control}
                      name="location"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Location</FormLabel>
                          <FormControl>
                            <Input placeholder="Enter location" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                  </div>
                </div>

                {/* Attachments */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold flex items-center gap-2">
                    <FileText className="h-4 w-4" />
                    Attachments
                  </h3>
                  
                  <FormField
                    control={form.control}
                    name="attachments"
                    render={({ field }) => (
                      <FormItem>
                        <FormLabel>Upload Files</FormLabel>
                        <FormControl>
                          <Input 
                            type="file" 
                            multiple
                            onChange={(e) => field.onChange(Array.from(e.target.files || []))}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                </div>

                <div className="flex gap-3 pt-4">
                  <Button type="submit" disabled={createLoading} className="flex-1">
                    {createLoading ? "Creating..." : "Create Case"}
                  </Button>
                  <Button type="button" variant="outline" onClick={() => setOpenDialog(false)} className="flex-1">
                    Cancel
                  </Button>
                </div>
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
                  className="border rounded-lg p-4 hover:shadow-md transition cursor-pointer"
                  onClick={() => navigate(`/cases/${caseItem.id}`)}
                >
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="font-medium text-lg flex-1">{caseItem.title}</h3>
                    <div className="flex gap-1 ml-2">
                      <Badge className={statusColors[caseItem.status]}>
                        {caseItem.status}
                      </Badge>
                      {caseItem.priority && (
                        <Badge className={priorityColors[caseItem.priority]}>
                          {caseItem.priority}
                        </Badge>
                      )}
                    </div>
                  </div>
                  
                  <p className="text-sm text-muted-foreground line-clamp-3 mb-3">
                    {caseItem.description}
                  </p>

                  {/* Additional Case Information */}
                  <div className="space-y-2 mb-3">
                    {caseItem.category && (
                      <div className="flex items-center gap-2 text-xs">
                        <Tag className="h-3 w-3" />
                        <Badge variant="outline" className={categoryColors[caseItem.category]}>
                          {caseItem.category}
                        </Badge>
                      </div>
                    )}
                    
                    {caseItem.assignedTo && (
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Users className="h-3 w-3" />
                        Assigned to: {caseItem.assignedTo.name}
                      </div>
                    )}
                    
                    {caseItem.dueDate && (
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <Calendar className="h-3 w-3" />
                        Due: {format(new Date(caseItem.dueDate), "MMM d, yyyy")}
                      </div>
                    )}
                    
                    {caseItem.budget && (
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <DollarSign className="h-3 w-3" />
                        Budget: ${caseItem.budget.toLocaleString()}
                      </div>
                    )}
                    
                    {caseItem.location && (
                      <div className="flex items-center gap-2 text-xs text-muted-foreground">
                        <MapPin className="h-3 w-3" />
                        {caseItem.location}
                      </div>
                    )}
                    
                    {caseItem.tags && caseItem.tags.length > 0 && (
                      <div className="flex items-center gap-2 text-xs">
                        <Tag className="h-3 w-3" />
                        <div className="flex gap-1 flex-wrap">
                          {caseItem.tags.slice(0, 3).map((tag, index) => (
                            <Badge key={index} variant="secondary" className="text-xs">
                              {tag}
                            </Badge>
                          ))}
                          {caseItem.tags.length > 3 && (
                            <Badge variant="secondary" className="text-xs">
                              +{caseItem.tags.length - 3}
                            </Badge>
                          )}
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="flex justify-between items-center text-xs text-muted-foreground">
                    <span>
                      Created{" "}
                      {format(new Date(caseItem.createdAt), "MMM d, yyyy")}
                    </span>
                    <Button variant="ghost" size="sm">
                      View Details
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
