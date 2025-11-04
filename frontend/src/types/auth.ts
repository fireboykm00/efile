export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  departmentId?: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export enum UserRole {
  ADMIN = "ADMIN",
  CEO = "CEO",
  CFO = "CFO",
  PROCUREMENT = "PROCUREMENT",
  ACCOUNTANT = "ACCOUNTANT",
  AUDITOR = "AUDITOR",
  IT = "IT",
  INVESTOR = "INVESTOR",
}

export interface UserSummaryResponse {
  user: User;
  permissions: string[];
}
