package com.efile.core.dashboard.dto;

public record DashboardSummaryResponse(
    // Basic counts
    int pendingDocumentsCount,
    int assignedCasesCount,
    int unreadCommunicationsCount,
    int overdueCasesCount,
    
    // Extended metrics
    int totalDocuments,
    int approvedDocuments,
    int rejectedDocuments,
    int activeCases,
    int unreadMessages,
    
    // Executive metrics (optional)
    Double monthlyGrowth,
    Double avgProcessingTime,
    Double efficiency
) {}