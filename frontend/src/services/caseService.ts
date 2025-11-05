import { apiClient } from "./api";
import {
  Case,
  CreateCaseRequest,
  UpdateCaseRequest,
  CaseStatus,
} from "@/types/case";

export interface CaseListResponse {
  cases: Case[];
  total: number;
  page: number;
  limit: number;
}

export const caseService = {
  async getCases(
    page = 1,
    limit = 10,
    status?: CaseStatus
  ): Promise<CaseListResponse> {
    // Backend doesn't support pagination parameters, returns all cases
    const response = await apiClient.get<Case[]>("/cases");
    // Backend returns List<CaseResponse>, wrap it in CaseListResponse format
    const cases = response.data || [];

    // Apply client-side filtering if status is provided
    const filteredCases = status
      ? cases.filter((c) => c.status === status)
      : cases;

    // Apply client-side pagination
    const startIndex = (page - 1) * limit;
    const paginatedCases = filteredCases.slice(startIndex, startIndex + limit);

    return {
      cases: paginatedCases,
      total: filteredCases.length,
      page,
      limit,
    };
  },

  async getCaseById(id: string): Promise<Case> {
    const response = await apiClient.get<Case>(`/cases/${id}`);
    return response.data;
  },

  async getCaseWithDocuments(id: string): Promise<Case> {
    const response = await apiClient.get<Case>(`/cases/${id}/details`);
    return response.data;
  },

  async updateCaseStatus(id: string, status: CaseStatus): Promise<Case> {
    const response = await apiClient.put<Case>(`/cases/${id}/status`, status);
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
    const response = await apiClient.put<Case>(`/cases/${id}/assign`, {
      userId,
    });
    return response.data;
  },
};
