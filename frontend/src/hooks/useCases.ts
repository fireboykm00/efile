import { useState, useEffect } from "react";
import { caseService, CaseListResponse } from "@/services/caseService";
import { Case, CreateCaseRequest, UpdateCaseRequest, CaseStatus } from "@/types/case";

export function useCases(page = 1, status?: CaseStatus) {
  const [data, setData] = useState<CaseListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const refetch = async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await caseService.getCases(page, 10, status);
      setData(result);
    } catch (err) {
      setError(err instanceof Error ? err : new Error("Failed to fetch cases"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refetch();
  }, [page, status]);

  return { data, loading, error, refetch };
}

export function useCaseById(id: string) {
  const [data, setData] = useState<Case | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const fetchCase = async () => {
      try {
        setLoading(true);
        setError(null);
        const caseData = await caseService.getCaseById(id);
        setData(caseData);
      } catch (err) {
        setError(err instanceof Error ? err : new Error("Failed to fetch case"));
      } finally {
        setLoading(false);
      }
    };

    fetchCase();
  }, [id]);

  return { data, loading, error };
}

export function useCaseActions() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const createCase = async (request: CreateCaseRequest): Promise<Case | null> => {
    try {
      setLoading(true);
      setError(null);
      const caseData = await caseService.createCase(request);
      return caseData;
    } catch (err) {
      setError(err instanceof Error ? err : new Error("Failed to create case"));
      return null;
    } finally {
      setLoading(false);
    }
  };

  const updateCase = async (id: string, request: UpdateCaseRequest): Promise<Case | null> => {
    try {
      setLoading(true);
      setError(null);
      const caseData = await caseService.updateCase(id, request);
      return caseData;
    } catch (err) {
      setError(err instanceof Error ? err : new Error("Failed to update case"));
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { createCase, updateCase, loading, error };
}
