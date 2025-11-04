import type {
  LoginRequest,
  TokenResponse,
  RefreshTokenRequest,
  UserSummaryResponse,
} from "@/types/auth";
import { apiClient } from "./api";

export class AuthService {
  static async login(credentials: LoginRequest): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>(
      "/auth/login",
      credentials
    );

    const tokenData = response.data!;
    this.storeTokens(tokenData);

    return tokenData;
  }

  static async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await apiClient.post<TokenResponse>("/auth/refresh", {
      refreshToken,
    } as RefreshTokenRequest);

    const tokenData = response.data!;
    this.storeTokens(tokenData);

    return tokenData;
  }

  static async logout(): Promise<void> {
    try {
      await apiClient.post("/auth/logout");
    } catch {
      // Ignore logout errors
    } finally {
      this.clearTokens();
    }
  }

  static async getCurrentUser(): Promise<UserSummaryResponse> {
    const response = await apiClient.get<UserSummaryResponse>("/users/me");
    return response.data!;
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

  static isAuthenticated(): boolean {
    const token = localStorage.getItem("accessToken");
    if (!token) return false;

    try {
      // Basic check - in production you'd validate the token
      return true;
    } catch {
      return false;
    }
  }

  static getAccessToken(): string | null {
    return localStorage.getItem("accessToken");
  }

  static getRefreshToken(): string | null {
    return localStorage.getItem("refreshToken");
  }

  private static storeTokens(tokenData: TokenResponse): void {
    localStorage.setItem("accessToken", tokenData.accessToken);
    localStorage.setItem("refreshToken", tokenData.refreshToken);
  }

  private static clearTokens(): void {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
  }
}
