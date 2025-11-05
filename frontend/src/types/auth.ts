export interface LoginRequest {
  email: string;
  password: string;
}

export interface User {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  isActive: boolean;
  status?: "ACTIVE" | "INACTIVE";
  department?: string;
  phone?: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  user: User | null;
  token: string | null;
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
  id: number;
  name: string;
  email: string;
  role: string;
}
