import { useState, useEffect } from "react";
import { documentService, DocumentListResponse } from "@/services/documentService";
import { Document, DocumentSearchQuery } from "@/types/document";

export function useDocuments(query?: DocumentSearchQuery, skip?: boolean) {
  const [data, setData] = useState<DocumentListResponse | null>(null);
  const [loading, setLoading] = useState(!skip);
  const [error, setError] = useState<Error | null>(null);

  const refetch = async () => {
    try {
      setLoading(true);
      setError(null);
      const result = await documentService.getDocuments(query);
      setData(result);
    } catch (err) {
      setError(
        err instanceof Error ? err : new Error("Failed to fetch documents")
      );
    } finally {
      setLoading(false);
    }
  };

  const approveDocument = async (id: string): Promise<void> => {
    try {
      await documentService.approveDocument(id);
    } catch (err) {
      throw err instanceof Error ? err : new Error("Failed to approve document");
    }
  };

  const rejectDocument = async (id: string, reason: string): Promise<void> => {
    try {
      await documentService.rejectDocument(id, reason);
    } catch (err) {
      throw err instanceof Error ? err : new Error("Failed to reject document");
    }
  };

  useEffect(() => {
    if (skip) return;
    refetch();
  }, [query?.page, query?.status, query?.type, query?.caseId, skip]);

  return { data, loading, error, refetch, approveDocument, rejectDocument };
}

export function useDocumentById(id: string, skip?: boolean) {
  const [data, setData] = useState<Document | null>(null);
  const [loading, setLoading] = useState(!skip);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (skip) return;

    const fetchDocument = async () => {
      try {
        setLoading(true);
        setError(null);
        const document = await documentService.getDocumentById(id);
        setData(document);
      } catch (err) {
        setError(
          err instanceof Error ? err : new Error("Failed to fetch document")
        );
      } finally {
        setLoading(false);
      }
    };

    fetchDocument();
  }, [id, skip]);

  return { data, loading, error };
}

export function useDocumentUpload() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [progress, setProgress] = useState(0);

  const uploadDocument = async (
    title: string,
    type: string,
    caseId: string,
    file: File
  ): Promise<Document | null> => {
    try {
      setLoading(true);
      setError(null);
      setProgress(0);

      // Simulate progress
      const progressInterval = setInterval(() => {
        setProgress((prev: number) => Math.min(prev + 10, 90));
      }, 200);

      const document = await documentService.uploadDocument(
        title,
        type,
        caseId,
        file
      );

      clearInterval(progressInterval);
      setProgress(100);
      return document;
    } catch (err) {
      setError(
        err instanceof Error ? err : new Error("Failed to upload document")
      );
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { uploadDocument, loading, error, progress };
}

export function useDocumentApproval() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const approveDocument = async (id: string): Promise<Document | null> => {
    try {
      setLoading(true);
      setError(null);
      const document = await documentService.approveDocument(id);
      return document;
    } catch (err) {
      setError(
        err instanceof Error ? err : new Error("Failed to approve document")
      );
      return null;
    } finally {
      setLoading(false);
    }
  };

  const rejectDocument = async (
    id: string,
    reason: string
  ): Promise<Document | null> => {
    try {
      setLoading(true);
      setError(null);
      const document = await documentService.rejectDocument(id, reason);
      return document;
    } catch (err) {
      setError(
        err instanceof Error ? err : new Error("Failed to reject document")
      );
      return null;
    } finally {
      setLoading(false);
    }
  };

  return { approveDocument, rejectDocument, loading, error };
}

export function useDocumentSearch(searchQuery: string, skip?: boolean) {
  const [data, setData] = useState<DocumentListResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (skip || !searchQuery) {
      setData(null);
      return;
    }

    const fetchResults = async () => {
      try {
        setLoading(true);
        setError(null);
        const results = await documentService.searchDocuments(searchQuery);
        setData(results);
      } catch (err) {
        setError(
          err instanceof Error ? err : new Error("Search failed")
        );
      } finally {
        setLoading(false);
      }
    };

    const debounceTimer = setTimeout(fetchResults, 300);
    return () => clearTimeout(debounceTimer);
  }, [searchQuery, skip]);

  return { data, loading, error };
}
