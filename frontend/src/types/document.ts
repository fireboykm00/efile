import { User } from "./user";
import { Case } from "./case";

export enum DocumentStatus {
  DRAFT = "DRAFT",
  SUBMITTED = "SUBMITTED",
  UNDER_REVIEW = "UNDER_REVIEW",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
  WITHDRAWN = "WITHDRAWN",
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
  caseTitle?: string;
  case?: Case;
  uploadedById: string;
  uploadedByName?: string;
  uploadedBy?: User;
  approvedById?: string;
  approvedByName?: string;
  approvedBy?: User;
  rejectionReason?: string;
  receiptNumber?: string;
  uploadedAt: Date;
  processedAt?: Date;
  createdAt?: Date;
  updatedAt?: Date;
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
