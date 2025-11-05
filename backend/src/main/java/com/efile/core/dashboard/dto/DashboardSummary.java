package com.efile.core.dashboard.dto;

public record DashboardSummary(
    // Basic counts
    long pendingDocumentsCount,
    long assignedCasesCount,
    long unreadCommunicationsCount,
    long overdueCasesCount,

    // Extended metrics
    long totalDocuments,
    long approvedDocuments,
    long rejectedDocuments,
    long activeCases,
    long unreadMessages,

    // Executive metrics (optional)
    Double monthlyGrowth,
    Double avgProcessingTime,
    Double efficiency,

    // Admin-only metrics
    Long totalUsers
) {}