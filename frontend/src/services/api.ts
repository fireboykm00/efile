import axios from "axios";
import type { AxiosInstance, AxiosResponse, AxiosError } from "axios";
import { toast } from "sonner";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

class ApiClient {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor to add JWT token
    this.instance.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("authToken");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for error handling
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => response,
      async (error: AxiosError<{ message?: string; error?: string }>) => {
        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
          // Clear token and redirect to login
          localStorage.removeItem("authToken");
          window.location.href = "/login";
        }

        // Handle other errors with toast notifications
        this.handleError(error);
        return Promise.reject(error);
      }
    );
  }

  private handleError(
    error: AxiosError<{ message?: string; error?: string }>
  ): void {
    if (error.response) {
      const message =
        error.response.data?.message ||
        error.response.data?.error ||
        "An error occurred";
      toast.error(message);
    } else if (error.request) {
      toast.error("Network error. Please check your connection.");
    } else {
      toast.error("An unexpected error occurred.");
    }
  }

  public async get<T>(
    url: string,
    params?: Record<string, unknown>
  ): Promise<AxiosResponse<T>> {
    return this.instance.get(url, { params });
  }

  public async post<T>(url: string, data?: unknown): Promise<AxiosResponse<T>> {
    return this.instance.post(url, data);
  }

  public async put<T>(url: string, data?: unknown): Promise<AxiosResponse<T>> {
    return this.instance.put(url, data);
  }

  public async delete<T>(url: string): Promise<AxiosResponse<T>> {
    return this.instance.delete(url);
  }
}

export const apiClient = new ApiClient();
