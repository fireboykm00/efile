import { User } from "./user";
import { Case } from "./case";

export enum CommunicationType {
  MESSAGE = "MESSAGE",
  NOTIFICATION = "NOTIFICATION",
  SYSTEM_ALERT = "SYSTEM_ALERT",
}

export interface Communication {
  id: string;
  type: CommunicationType;
  content: string;
  isRead: boolean;
  senderId: string;
  sender?: User;
  recipientId: string;
  recipient?: User;
  caseId?: string;
  case?: Case;
  sentAt: string;
  readAt?: string;
}

export interface SendCommunicationRequest {
  type: CommunicationType;
  content: string;
  recipientId: string;
  caseId?: string;
}

export interface MarkAsReadRequest {
  isRead: boolean;
}
