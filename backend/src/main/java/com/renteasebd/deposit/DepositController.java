package com.renteasebd.deposit;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.deposit.dto.CreateDepositTransactionRequest;
import com.renteasebd.deposit.dto.DepositBalanceResponse;
import com.renteasebd.deposit.dto.DepositLedgerResponse;
import com.renteasebd.deposit.dto.DepositTransactionResponse;
import com.renteasebd.deposit.dto.MoveOutSettlementRequest;
import com.renteasebd.deposit.dto.MoveOutSettlementResponse;
import com.renteasebd.export.PdfService;
import com.renteasebd.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/deposits")
public class DepositController {

    private final DepositService depositService;
    private final PdfService pdfService;

    public DepositController(DepositService depositService, PdfService pdfService) {
        this.depositService = depositService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<DepositTransactionResponse>> list(
        @RequestParam(required = false) Long tenantId,
        @RequestParam(required = false) Long propertyId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(depositService.list(principal.id(), principal.role(), tenantId, propertyId));
    }

    @GetMapping("/tenants/{tenantId}/balance")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<DepositBalanceResponse> getBalance(
        @PathVariable Long tenantId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(depositService.getBalance(tenantId, principal.id(), principal.role()));
    }

    @GetMapping("/tenants/{tenantId}/ledger")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<DepositLedgerResponse> getLedger(
        @PathVariable Long tenantId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(depositService.getLedger(tenantId, principal.id(), principal.role()));
    }

    @PostMapping("/tenants/{tenantId}/transactions")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<DepositTransactionResponse> createTransaction(
        @PathVariable Long tenantId,
        @Valid @RequestBody CreateDepositTransactionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(depositService.createTransaction(tenantId, request, principal.id(), principal.role()));
    }

    @PostMapping("/tenants/{tenantId}/move-out-settlement")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<MoveOutSettlementResponse> settleMoveOut(
        @PathVariable Long tenantId,
        @Valid @RequestBody MoveOutSettlementRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(depositService.settleMoveOut(tenantId, request, principal.id(), principal.role()));
    }

    @GetMapping("/{id}/receipt-pdf")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> receiptPdf(
        @PathVariable Long id,
        @RequestParam(defaultValue = "en") String lang,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PdfService.GeneratedPdf pdf = pdfService.generateDepositRefundReceiptPdf(id, lang, principal.id(), principal.role());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=deposit-refund-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF);
        if (pdf.fallbackToEnglish()) {
            builder.header("X-Language-Fallback", "en");
        }
        return builder.body(pdf.bytes());
    }
}
