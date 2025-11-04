import { User } from "./user";
import { Document } from "./document";
import { Communication } from "./communication";

export enum CaseStatus {
  OPEN = "OPEN",
  IN_PROGRESS = "IN_PROGRESS",
  CLOSED = "CLOSED",
  ARCHIVED = "ARCHIVED",
}

export interface Case {
  id: string;
  title: string;
  description: string;
  status: CaseStatus;
  assignedToId?: string;
  assignedTo?: User;
  createdById: string;
  createdBy?: User;
  documents?: Document[];
  communications?: Communication[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateCaseRequest {
  title: string;
  description: string;
  status?: CaseStatus;
  assignedToId?: string;
}

export interface UpdateCaseRequest {
  title?: string;
  description?: string;
  status?: CaseStatus;
  assignedToId?: string;
}
