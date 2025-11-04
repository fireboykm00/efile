package com.efile.core.export;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/documents")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR','INVESTOR')")
    public ResponseEntity<byte[]> exportDocuments(@RequestParam(value = "format", defaultValue = "csv") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = exportService.exportDocumentsAsCsv();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=documents.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
        }
        
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    @GetMapping("/cases")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR')")
    public ResponseEntity<byte[]> exportCases(@RequestParam(value = "format", defaultValue = "csv") String format) {
        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = exportService.exportCasesAsCsv();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cases.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
        }
        
        throw new IllegalArgumentException("Unsupported format: " + format);
    }
}
