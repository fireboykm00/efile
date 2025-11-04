import { apiClient } from "./api";
import { Communication, SendCommunicationRequest, CommunicationType } from "@/types/communication";

export interface CommunicationListResponse {
  communications: Communication[];
  total: number;
  unreadCount: number;
}

export const communicationService = {
  async getCommunications(limit = 20): Promise<CommunicationListResponse> {
    const response = await apiClient.get<CommunicationListResponse>(
      "/communications",
      { params: { limit } }
    );
    return response.data;
  },

  async getCommunicationById(id: string): Promise<Communication> {
    const response = await apiClient.get<Communication>(`/communications/${id}`);
    return response.data;
  },

  async sendCommunication(request: SendCommunicationRequest): Promise<Communication> {
    const response = await apiClient.post<Communication>("/communications", request);
    return response.data;
  },

  async markAsRead(id: string): Promise<Communication> {
    const response = await apiClient.put<Communication>(
      `/communications/${id}/read`,
      { isRead: true }
    );
    return response.data;
  },

  async getUnreadCount(): Promise<number> {
    const response = await apiClient.get<{ count: number }>(
      "/communications/unread-count"
    );
    return response.data.count;
  },
};
