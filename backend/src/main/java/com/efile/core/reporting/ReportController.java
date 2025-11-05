package com.efile.core.reporting;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.lang.NonNull;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/documents/export")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR')")
    public ResponseEntity<String> exportDocumentsReport(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String report = reportService.generateDocumentsReport(format);
        String filename = "documents_report_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(report);
    }

    @GetMapping("/cases/export")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR')")
    public ResponseEntity<String> exportCasesReport(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String report = reportService.generateCasesReport(format);
        String filename = "cases_report_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(report);
    }

    @GetMapping("/users/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportUsersReport(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String report = reportService.generateUsersReport(format);
        String filename = "users_report_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(report);
    }

    @GetMapping("/communications/export")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR')")
    public ResponseEntity<String> exportCommunicationsReport(@RequestParam(value = "format", defaultValue = "csv") String format) {
        String report = reportService.generateCommunicationsReport(format);
        String filename = "communications_report_" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(report);
    }
}
