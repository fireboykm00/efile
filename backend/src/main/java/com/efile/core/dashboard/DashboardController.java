package com.efile.core.dashboard;

import com.efile.core.casemanagement.Case;
import com.efile.core.communication.Communication;
import com.efile.core.dashboard.dto.DashboardSummary;
import com.efile.core.document.Document;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardSummary> getSummary() {
        DashboardSummary summary = dashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pending-documents")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO')")
    public ResponseEntity<List<Document>> getPendingDocuments(
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        List<Document> documents = dashboardService.getPendingDocuments();
        return ResponseEntity.ok(documents.stream().limit(limit).toList());
    }

    @GetMapping("/assigned-cases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Case>> getAssignedCases(
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        List<Case> cases = dashboardService.getAssignedCases();
        return ResponseEntity.ok(cases.stream().limit(limit).toList());
    }

    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Communication>> getNotifications(
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        List<Communication> notifications = dashboardService.getNotifications();
        return ResponseEntity.ok(notifications.stream().limit(limit).toList());
    }
}
