import { User } from "./user";
import { Document } from "./document";
import { Communication } from "./communication";

export enum CaseStatus {
  OPEN = "OPEN",
  ACTIVE = "ACTIVE",
  UNDER_REVIEW = "UNDER_REVIEW",
  COMPLETED = "COMPLETED",
  ON_HOLD = "ON_HOLD",
  CLOSED = "CLOSED",
}

export enum CasePriority {
  LOW = "LOW",
  MEDIUM = "MEDIUM",
  HIGH = "HIGH",
  URGENT = "URGENT",
}

export enum CaseCategory {
  GENERAL = "GENERAL",
  LEGAL = "LEGAL",
  FINANCIAL = "FINANCIAL",
  HR = "HR",
  COMPLIANCE = "COMPLIANCE",
  OPERATIONS = "OPERATIONS",
  STRATEGIC = "STRATEGIC",
}

export interface Case {
  id: string;
  title: string;
  description: string;
  status: CaseStatus;
  priority: CasePriority;
  category: CaseCategory;
  tags: string[];
  assignedToId?: string;
  assignedTo?: User;
  createdById: string;
  createdBy?: User;
  documents?: Document[];
  communications?: Communication[];
  dueDate?: string;
  estimatedCompletionDate?: string;
  actualCompletionDate?: string;
  budget?: number;
  actualCost?: number;
  location?: string;
  department?: string;
  stakeholderIds?: string[];
  stakeholders?: User[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateCaseRequest {
  title: string;
  description: string;
  status?: CaseStatus;
  priority?: CasePriority;
  category?: CaseCategory;
  tags?: string[];
  assignedToId?: string;
  dueDate?: string;
  estimatedCompletionDate?: string;
  budget?: number;
  location?: string;
  department?: string;
  stakeholderIds?: string[];
  attachments?: File[];
}

export interface UpdateCaseRequest {
  title?: string;
  description?: string;
  status?: CaseStatus;
  priority?: CasePriority;
  category?: CaseCategory;
  tags?: string[];
  assignedToId?: string;
  dueDate?: string;
  estimatedCompletionDate?: string;
  actualCompletionDate?: string;
  budget?: number;
  actualCost?: number;
  location?: string;
  department?: string;
  stakeholderIds?: string[];
}
