export interface DashboardSummary {
  totalDocuments: number;
  pendingDocuments: number;
  approvedDocuments: number;
  rejectedDocuments: number;
  totalCases: number;
  activeCases: number;
  closedCases: number;
  unreadCommunications: number;
}

export interface DashboardStats {
  label: string;
  value: number;
  change?: number;
  changeType?: 'increase' | 'decrease';
  icon?: string;
}

