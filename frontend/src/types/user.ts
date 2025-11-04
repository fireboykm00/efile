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

export interface Department {
  id: string;
  name: string;
  headId?: string;
  createdAt: string;
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  departmentId?: string;
  department?: Department;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
  departmentId?: string;
}

export interface UpdateUserRequest {
  name?: string;
  email?: string;
  role?: UserRole;
  departmentId?: string;
}
