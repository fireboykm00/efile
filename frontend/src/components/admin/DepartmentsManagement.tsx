import { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
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
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Plus, Edit, Trash2, Users } from "lucide-react";
import { toast } from "sonner";

const departmentSchema = z.object({
  name: z.string().min(2, "Department name must be at least 2 characters"),
  headId: z.string().optional(),
});

type DepartmentFormData = z.infer<typeof departmentSchema>;

interface Department {
  id: number;
  name: string;
  head?: {
    id: number;
    name: string;
    email: string;
    role: string;
  };
  userCount?: number;
}

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
}

export function DepartmentsManagement() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState<Department | null>(
    null
  );

  const form = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
  });

  useEffect(() => {
    loadDepartments();
    loadUsers();
  }, []);

  const loadDepartments = async () => {
    try {
      // Mock data for now - replace with actual API call
      const mockDepartments: Department[] = [
        {
          id: 1,
          name: "Executive",
          head: {
            id: 2,
            name: "John CEO",
            email: "ceo@efile.com",
            role: "CEO",
          },
          userCount: 1,
        },
        {
          id: 2,
          name: "Finance",
          head: {
            id: 3,
            name: "Jane CFO",
            email: "cfo@efile.com",
            role: "CFO",
          },
          userCount: 1,
        },
        { id: 3, name: "IT", userCount: 1 },
        { id: 4, name: "Procurement", userCount: 0 },
        { id: 5, name: "Accounting", userCount: 0 },
        { id: 6, name: "Audit", userCount: 0 },
        { id: 7, name: "Legal", userCount: 0 },
      ];
      setDepartments(mockDepartments);
    } catch (error) {
      console.error("Failed to load departments:", error);
      toast.error("Failed to load departments");
    }
  };

  const loadUsers = async () => {
    try {
      // Mock data for now - replace with actual API call
      const mockUsers: User[] = [
        { id: 2, name: "John CEO", email: "ceo@efile.com", role: "CEO" },
        { id: 3, name: "Jane CFO", email: "cfo@efile.com", role: "CFO" },
        {
          id: 1,
          name: "System Admin",
          email: "admin@efile.com",
          role: "ADMIN",
        },
      ];
      setUsers(mockUsers);
    } catch (error) {
      console.error("Failed to load users:", error);
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (data: DepartmentFormData) => {
    try {
      if (editingDepartment) {
        // Update department
        setDepartments(prev =>
          prev.map(dep =>
            dep.id === editingDepartment.id
              ? {
                  ...dep,
                  name: data.name,
                  head: data.headId ? users.find(u => u.id.toString() === data.headId) : undefined
                }
              : dep
          )
        );
        toast.success("Department updated successfully");
      } else {
        // Create department
        const newId = Math.max(...departments.map(d => d.id), 0) + 1;
        const newDept: Department = {
          id: newId,
          name: data.name,
          head: data.headId ? users.find(u => u.id.toString() === data.headId) : undefined,
          userCount: 0
        };
        setDepartments(prev => [...prev, newDept]);
        toast.success("Department created successfully");
      }
      form.reset();
      setOpenDialog(false);
      setEditingDepartment(null);
    } catch (error) {
      console.error("Failed to save department:", error);
      toast.error("Failed to save department");
    }
  };

  const handleEdit = (department: Department) => {
    setEditingDepartment(department);
    form.setValue("name", department.name);
    form.setValue("headId", department.head?.id.toString() || "");
    setOpenDialog(true);
  };

  const handleDelete = async (department: Department) => {
    if (department.userCount && department.userCount > 0) {
      toast.error("Cannot delete department with assigned users");
      return;
    }

    try {
      // Delete department - replace with actual API call
      setDepartments(prev => prev.filter(dep => dep.id !== department.id));
      toast.success("Department deleted successfully");
    } catch (error) {
      console.error("Failed to delete department:", error);
      toast.error("Failed to delete department");
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">Departments Management</h2>
        <Dialog
          open={openDialog}
          onOpenChange={(open) => {
            setOpenDialog(open);
            if (!open) {
              setEditingDepartment(null);
              form.reset();
            }
          }}
        >
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Add Department
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {editingDepartment
                  ? "Edit Department"
                  : "Create New Department"}
              </DialogTitle>
            </DialogHeader>
            <Form {...form}>
              <form
                onSubmit={form.handleSubmit(onSubmit)}
                className="space-y-4"
              >
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Department Name</FormLabel>
                      <FormControl>
                        <Input placeholder="Enter department name" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="headId"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Department Head (Optional)</FormLabel>
                      <Select
                        onValueChange={field.onChange}
                        value={field.value}
                      >
                        <FormControl>
                          <SelectTrigger>
                            <SelectValue placeholder="Select department head" />
                          </SelectTrigger>
                        </FormControl>
                        <SelectContent>
                          {users.map((user) => (
                            <SelectItem
                              key={user.id}
                              value={user.id.toString()}
                            >
                              {user.name} ({user.role})
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Button type="submit" className="w-full">
                  {editingDepartment
                    ? "Update Department"
                    : "Create Department"}
                </Button>
              </form>
            </Form>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardContent className="pt-6">
          {loading ? (
            <p className="text-muted-foreground">Loading departments...</p>
          ) : (
            <div className="space-y-3">
              {departments.map((department) => (
                <div
                  key={department.id}
                  className="flex justify-between items-center p-4 border rounded-lg hover:bg-slate-50"
                >
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="font-medium">{department.name}</h3>
                      {department.head && (
                        <Badge variant="outline">
                          Head: {department.head.name}
                        </Badge>
                      )}
                    </div>
                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span className="flex items-center gap-1">
                        <Users className="h-4 w-4" />
                        {department.userCount || 0} users
                      </span>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleEdit(department)}
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => handleDelete(department)}
                      disabled={(department.userCount || 0) > 0}
                    >
                      <Trash2 className="w-4 h-4" />
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
