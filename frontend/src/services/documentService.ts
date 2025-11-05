import { apiClient } from "./api";
import {
  Document,
  DocumentSearchQuery,
  DocumentStatus,
} from "@/types/document";

export interface DocumentListResponse {
  documents: Document[];
  total: number;
  page: number;
  limit: number;
}

export const documentService = {
  async getDocuments(
    query?: DocumentSearchQuery
  ): Promise<DocumentListResponse> {
    const response = await apiClient.get<any>("/documents", {
      params: query,
    });
    // Handle PageResponse format from backend
    const pageData = response.data;
    return {
      documents: pageData.content || [],
      total: pageData.totalElements || 0,
      page: pageData.number || 0,
      limit: pageData.size || 20,
    };
  },

  async getDocumentById(id: string): Promise<Document> {
    const response = await apiClient.get<Document>(`/documents/${id}`);
    return response.data;
  },

  async uploadDocument(
    title: string,
    type: string,
    caseId: string,
    file: File
  ): Promise<Document> {
    const formData = new FormData();
    formData.append("title", title);
    formData.append("type", type);
    formData.append("caseId", caseId);
    formData.append("file", file);

    // Use instance directly to avoid Content-Type header override
    const response = await apiClient.postFormData<Document>(
      "/documents/upload",
      formData
    );
    return response.data;
  },

  async downloadDocument(id: string): Promise<Blob> {
    const response = await apiClient.get(`/documents/${id}/download`, {
      responseType: "blob",
    });
    return response.data;
  },

  async approveDocument(id: string): Promise<Document> {
    const response = await apiClient.put<Document>(
      `/documents/${id}/approve`,
      {}
    );
    return response.data;
  },

  async rejectDocument(id: string, rejectionReason: string): Promise<Document> {
    const response = await apiClient.put<Document>(`/documents/${id}/reject`, {
      rejectionReason,
    });
    return response.data;
  },

  async searchDocuments(
    query: string,
    filters?: Partial<DocumentSearchQuery>
  ): Promise<DocumentListResponse> {
    return this.getDocuments({
      ...filters,
      query,
    });
  },

  async getDocumentReceipt(id: string): Promise<Blob> {
    const response = await apiClient.get(`/documents/${id}/receipt`, {
      responseType: "blob",
    });
    return response.data;
  },

  async getCaseDocuments(caseId: string): Promise<Document[]> {
    const response = await apiClient.get<Document[]>(
      `/cases/${caseId}/documents`
    );
    return response.data;
  },
};
