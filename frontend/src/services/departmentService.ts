import { apiClient } from "./api";

export interface Department {
  id: string;
  name: string;
  headId?: string;
  head?: {
    id: string;
    name: string;
    email: string;
  };
  createdAt: string;
  updatedAt: string;
}

export const departmentService = {
  async getDepartments(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>("/departments");
    return response.data;
  },

  async getDepartmentById(id: string): Promise<Department> {
    const response = await apiClient.get<Department>(`/departments/${id}`);
    return response.data;
  },
};
