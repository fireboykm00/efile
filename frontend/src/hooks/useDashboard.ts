import { useState, useEffect } from "react";
import { dashboardService, DashboardData } from "@/services/dashboardService";

interface UseDashboardOptions {
  enabled?: boolean;
  refetchInterval?: number;
}

export function useDashboard(options: UseDashboardOptions = {}) {
  const { enabled = true, refetchInterval = 30000 } = options;

  const [data, setData] = useState<DashboardData | null>(null);
  const [loading, setLoading] = useState(enabled);
  const [error, setError] = useState<Error | null>(null);

  const fetchDashboard = async () => {
    try {
      setLoading(true);
      setError(null);
      const dashboardData = await dashboardService.getFullDashboard();
      setData(dashboardData);
    } catch (err) {
      setError(
        err instanceof Error ? err : new Error("Failed to fetch dashboard")
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!enabled) return;

    fetchDashboard();

    // Set up polling
    const interval = setInterval(fetchDashboard, refetchInterval);

    return () => clearInterval(interval);
  }, [enabled, refetchInterval]);

  return {
    data,
    loading,
    error,
    refetch: fetchDashboard,
  };
}

export function usePendingDocuments(limit: number = 5) {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const documents = await dashboardService.getPendingDocuments(limit);
        setData(documents);
      } catch (err) {
        setError(
          err instanceof Error ? err : new Error("Failed to fetch documents")
        );
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [limit]);

  return { data, loading, error };
}

export function useAssignedCases(limit: number = 5) {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const cases = await dashboardService.getAssignedCases(limit);
        setData(cases);
      } catch (err) {
        setError(
          err instanceof Error ? err : new Error("Failed to fetch cases")
        );
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [limit]);

  return { data, loading, error };
}

export function useNotifications(limit: number = 5) {
  const [data, setData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const notifications = await dashboardService.getNotifications(limit);
        setData(notifications);
      } catch (err) {
        setError(
          err instanceof Error
            ? err
            : new Error("Failed to fetch notifications")
        );
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [limit]);

  return { data, loading, error };
}
