import { User } from "./user";
import { Case } from "./case";

export enum DocumentStatus {
  PENDING = "PENDING",
  UNDER_REVIEW = "UNDER_REVIEW",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
  ARCHIVED = "ARCHIVED",
}

export enum DocumentType {
  FINANCIAL_REPORT = "FINANCIAL_REPORT",
  PROCUREMENT_BID = "PROCUREMENT_BID",
  LEGAL_DOCUMENT = "LEGAL_DOCUMENT",
  AUDIT_REPORT = "AUDIT_REPORT",
  INVESTMENT_REPORT = "INVESTMENT_REPORT",
  GENERAL = "GENERAL",
}

export interface Document {
  id: string;
  title: string;
  type: DocumentType;
  filePath: string;
  fileSize: number;
  status: DocumentStatus;
  caseId: string;
  case?: Case;
  uploadedById: string;
  uploadedBy?: User;
  approvedById?: string;
  approvedBy?: User;
  rejectionReason?: string;
  receiptNumber?: string;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentSearchQuery {
  status?: DocumentStatus;
  type?: DocumentType;
  caseId?: string;
  startDate?: string;
  endDate?: string;
  query?: string;
  page?: number;
  limit?: number;
}

export interface UploadDocumentRequest {
  title: string;
  type: DocumentType;
  caseId: string;
  file: File;
}

export interface ApproveDocumentRequest {
  approvedById: string;
}

export interface RejectDocumentRequest {
  rejectionReason: string;
  approvedById: string;
}
