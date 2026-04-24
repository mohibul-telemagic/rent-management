package com.renteasebd.export;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.domain.user.UserRole;
import com.renteasebd.export.dto.CreateExportJobRequest;
import com.renteasebd.export.dto.ExportJobResponse;
import com.renteasebd.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/export")
public class ExportController {

    private final ExportCsvService exportCsvService;
    private final ExportJobService exportJobService;

    public ExportController(ExportCsvService exportCsvService, ExportJobService exportJobService) {
        this.exportCsvService = exportCsvService;
        this.exportJobService = exportJobService;
    }

    @GetMapping("/tenants")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> exportTenants(@AuthenticationPrincipal UserPrincipal principal) {
        return csvResponse("tenants.csv", exportCsvService.exportTenants(principal.id(), principal.role()));
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> exportInvoices(@AuthenticationPrincipal UserPrincipal principal) {
        return csvResponse("invoices.csv", exportCsvService.exportInvoices(principal.id(), principal.role()));
    }

    @GetMapping("/payments")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> exportPayments(@AuthenticationPrincipal UserPrincipal principal) {
        return csvResponse("payments.csv", exportCsvService.exportPayments(principal.id(), principal.role()));
    }

    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> exportExpenses(@AuthenticationPrincipal UserPrincipal principal) {
        return csvResponse("expenses.csv", exportCsvService.exportExpenses(principal.id(), principal.role()));
    }

    @GetMapping("/deposits")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> exportDeposits(@AuthenticationPrincipal UserPrincipal principal) {
        return csvResponse("deposits.csv", exportCsvService.exportDeposits(principal.id(), principal.role()));
    }

    @PostMapping("/jobs/zip")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExportJobResponse> createZipJob(
        @Valid @RequestBody(required = false) CreateExportJobRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(exportJobService.createZipJob(request, principal.id(), principal.role()));
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ExportJobResponse> getJob(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(exportJobService.getJob(id, principal.id()));
    }

    @GetMapping("/jobs/{id}/download")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> downloadJob(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) {
        byte[] bytes = exportJobService.downloadZip(id, principal.id());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=export-" + id + ".zip")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes);
    }

    private ResponseEntity<byte[]> csvResponse(String fileName, byte[] bytes) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }
}
