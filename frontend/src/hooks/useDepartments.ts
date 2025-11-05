import { useState, useEffect } from "react";
import { departmentService, Department } from "@/services/departmentService";

export function useDepartments() {
  const [data, setData] = useState<Department[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchDepartments = async () => {
    try {
      setLoading(true);
      setError(null);
      const departments = await departmentService.getDepartments();
      setData(departments);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to fetch departments");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, []);

  return {
    data,
    loading,
    error,
    refetch: fetchDepartments,
  };
}
