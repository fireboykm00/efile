import { apiClient } from "./api";
import { Case, CreateCaseRequest, UpdateCaseRequest, CaseStatus } from "@/types/case";

export interface CaseListResponse {
  cases: Case[];
  total: number;
  page: number;
  limit: number;
}

export const caseService = {
  async getCases(page = 1, limit = 10, status?: CaseStatus): Promise<CaseListResponse> {
    const response = await apiClient.get<CaseListResponse>("/cases", {
      params: { page, limit, status },
    });
    return response.data;
  },

  async getCaseById(id: string): Promise<Case> {
    const response = await apiClient.get<Case>(`/cases/${id}`);
    return response.data;
  },

  async createCase(request: CreateCaseRequest): Promise<Case> {
    const response = await apiClient.post<Case>("/cases", request);
    return response.data;
  },

  async updateCase(id: string, request: UpdateCaseRequest): Promise<Case> {
    const response = await apiClient.put<Case>(`/cases/${id}`, request);
    return response.data;
  },

  async deleteCase(id: string): Promise<void> {
    await apiClient.delete(`/cases/${id}`);
  },

  async assignCase(id: string, userId: string): Promise<Case> {
    const response = await apiClient.put<Case>(`/cases/${id}/assign`, { userId });
    return response.data;
  },

  async archiveCase(id: string): Promise<Case> {
    const response = await apiClient.put<Case>(`/cases/${id}`, {
      status: CaseStatus.ARCHIVED,
    });
    return response.data;
  },
};
