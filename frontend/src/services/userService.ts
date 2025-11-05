import { apiClient } from "./api";
import {
  User,
  CreateUserRequest,
  UpdateUserRequest,
  UserRole,
} from "@/types/user";

export interface UserListResponse {
  users: User[];
  total: number;
}

export const userService = {
  async getUsers(role?: UserRole): Promise<UserListResponse> {
    const response = await apiClient.get<User[]>("/users", {
      params: role ? { role } : {},
    });
    // Backend returns List<UserResponse>, wrap it in UserListResponse format
    const users = response.data || [];
    return {
      users,
      total: users.length,
    };
  },

  async getUserById(id: string): Promise<User> {
    const response = await apiClient.get<User>(`/users/${id}`);
    return response.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>("/users/me");
    return response.data;
  },

  async createUser(request: CreateUserRequest): Promise<User> {
    const response = await apiClient.post<User>("/users", request);
    return response.data;
  },

  async updateUser(id: string, request: UpdateUserRequest): Promise<User> {
    const response = await apiClient.put<User>(`/users/${id}`, request);
    return response.data;
  },

  async deleteUser(id: string): Promise<void> {
    await apiClient.delete(`/users/${id}`);
  },

  async deactivateUser(id: string): Promise<User> {
    const response = await apiClient.put<User>(`/users/${id}`, {
      isActive: false,
    });
    return response.data;
  },

  async updateProfile(request: { name: string; email: string }): Promise<User> {
    const response = await apiClient.put<User>("/users/me", request);
    return response.data;
  },
};
