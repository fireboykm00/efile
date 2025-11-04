import { LoginRequest, LoginResponse, User } from "@/types/auth";
import { apiClient } from "./api";

export class AuthService {
  static async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
      "/auth/login",
      credentials
    );
    return response.data;
  }

  static async logout(): Promise<void> {
    await apiClient.post("/auth/logout");
  }

  static async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<User>("/auth/me");
    return response.data;
  }

  static async register(data: {
    name: string;
    email: string;
    password: string;
    role: string;
    departmentId?: number;
  }): Promise<void> {
    await apiClient.post("/auth/register", data);
  }
}

export const authService = AuthService;
