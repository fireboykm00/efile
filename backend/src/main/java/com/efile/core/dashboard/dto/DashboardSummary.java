package com.efile.core.dashboard.dto;

public record DashboardSummary(
    long pendingDocuments,
    long assignedCases,
    long unreadCommunications
) {}
