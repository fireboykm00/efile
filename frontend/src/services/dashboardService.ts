import { apiClient } from "./api";
import { Document } from "@/types/document";
import { Case } from "@/types/case";
import { Communication } from "@/types/communication";

export interface DashboardSummary {
  pendingDocumentsCount: number;
  assignedCasesCount: number;
  unreadCommunicationsCount: number;
  overdueCasesCount: number;
}

export interface DashboardData {
  summary: DashboardSummary;
  pendingDocuments: Document[];
  assignedCases: Case[];
  unreadCommunications: Communication[];
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

    return {
      summary,
      pendingDocuments,
      assignedCases,
      unreadCommunications,
    };
  },
};
