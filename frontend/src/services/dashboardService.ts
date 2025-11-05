import { apiClient } from "./api";
import { Document } from "@/types/document";
import { Case } from "@/types/case";
import { Communication } from "@/types/communication";

export interface DashboardSummary {
  pendingDocumentsCount: number;
  assignedCasesCount: number;
  unreadCommunicationsCount: number;
  overdueCasesCount: number;
  totalDocuments: number;
  approvedDocuments: number;
  rejectedDocuments: number;
  activeCases: number;
  unreadMessages: number;
  // Executive metrics
  monthlyGrowth?: number;
  avgProcessingTime?: number;
  efficiency?: number;
  // Admin-only metrics
  totalUsers?: number;
}

export interface DashboardData {
  summary: DashboardSummary;
  pendingDocumentsList: Document[];
  assignedCasesList: Case[];
  unreadCommunicationsList: Communication[];
  // Flattened summary data for easier access
  totalDocuments: number;
  approvedDocuments: number;
  rejectedDocuments: number;
  pendingDocumentsCount: number;
  activeCases: number;
  unreadMessages: number;
  overdueCasesCount: number;
  monthlyGrowth?: number;
  avgProcessingTime?: number;
  efficiency?: number;
  totalUsers?: number;
}

export const dashboardService = {
  async getSummary(): Promise<DashboardSummary> {
    const response = await apiClient.get<DashboardSummary>(
      "/dashboard/summary"
    );
    return response.data;
  },

  async getPendingDocuments(limit: number = 5): Promise<Document[]> {
    const response = await apiClient.get<Document[]>(
      "/dashboard/pending-documents",
      {
        params: { limit },
      }
    );
    return response.data;
  },

  async getAssignedCases(limit: number = 5): Promise<Case[]> {
    const response = await apiClient.get<Case[]>("/dashboard/assigned-cases", {
      params: { limit },
    });
    return response.data;
  },

  async getNotifications(limit: number = 5): Promise<Communication[]> {
    const response = await apiClient.get<Communication[]>(
      "/dashboard/notifications",
      {
        params: { limit },
      }
    );
    return response.data;
  },

  async getFullDashboard(): Promise<DashboardData> {
    const [summary, pendingDocuments, assignedCases, unreadCommunications] =
      await Promise.all([
        this.getSummary(),
        this.getPendingDocuments(),
        this.getAssignedCases(),
        this.getNotifications(),
      ]);

    // Flatten the data structure for easier component access
    return {
      summary,
      pendingDocumentsList: pendingDocuments,
      assignedCasesList: assignedCases,
      unreadCommunicationsList: unreadCommunications,
      // Flattened summary data
      totalDocuments: summary.totalDocuments,
      approvedDocuments: summary.approvedDocuments,
      rejectedDocuments: summary.rejectedDocuments,
      pendingDocumentsCount: summary.pendingDocumentsCount,
      activeCases: summary.activeCases,
      unreadMessages: summary.unreadMessages,
      overdueCasesCount: summary.overdueCasesCount,
      monthlyGrowth: summary.monthlyGrowth,
      avgProcessingTime: summary.avgProcessingTime,
      efficiency: summary.efficiency,
      totalUsers: summary.totalUsers,
    };
  },
};
