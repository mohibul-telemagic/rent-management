package com.renteasebd.invoice;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.domain.invoice.InvoiceStatus;
import com.renteasebd.export.PdfService;
import com.renteasebd.invoice.dto.ApplyFifoPaymentRequest;
import com.renteasebd.invoice.dto.ApplyFifoPaymentResponse;
import com.renteasebd.invoice.dto.BulkGenerateInvoicesRequest;
import com.renteasebd.invoice.dto.BulkGenerateInvoicesResponse;
import com.renteasebd.invoice.dto.CancelInvoiceRequest;
import com.renteasebd.invoice.dto.CreateInvoiceRequest;
import com.renteasebd.invoice.dto.GenerateInvoiceResponse;
import com.renteasebd.invoice.dto.InvoiceResponse;
import com.renteasebd.invoice.dto.RecordInvoicePaymentRequest;
import com.renteasebd.invoice.dto.RecordInvoicePaymentResponse;
import com.renteasebd.invoice.dto.VoidInvoiceRequest;
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
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PdfService pdfService;

    public InvoiceController(InvoiceService invoiceService, PdfService pdfService) {
        this.invoiceService = invoiceService;
        this.pdfService = pdfService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<InvoiceResponse>> list(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long tenantId,
        @RequestParam(required = false) Long propertyId,
        @RequestParam(required = false) InvoiceStatus status,
        @RequestParam(required = false) String billingMonth
    ) {
        return ApiResponse.ok(
            invoiceService.list(principal.id(), principal.role(), tenantId, propertyId, status, billingMonth)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<InvoiceResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(invoiceService.get(id, principal.id(), principal.role()));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ResponseEntity<byte[]> getPdf(
        @PathVariable Long id,
        @RequestParam(defaultValue = "en") String lang,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        PdfService.GeneratedPdf pdf = pdfService.generateInvoicePdf(id, lang, principal.id(), principal.role());
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF);
        if (pdf.fallbackToEnglish()) {
            builder.header("X-Language-Fallback", "en");
        }
        return builder.body(pdf.bytes());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<GenerateInvoiceResponse> create(
        @Valid @RequestBody CreateInvoiceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.generateSingle(request, principal.id(), principal.role()));
    }

    @PostMapping("/bulk-generate")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<BulkGenerateInvoicesResponse> bulkGenerate(
        @Valid @RequestBody BulkGenerateInvoicesRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.generateBulk(request, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<InvoiceResponse> send(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(invoiceService.send(id, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<InvoiceResponse> cancel(
        @PathVariable Long id,
        @Valid @RequestBody CancelInvoiceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.cancel(id, request, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<InvoiceResponse> voidInvoice(
        @PathVariable Long id,
        @Valid @RequestBody VoidInvoiceRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.voidInvoice(id, request, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<RecordInvoicePaymentResponse> recordPayment(
        @PathVariable Long id,
        @Valid @RequestBody RecordInvoicePaymentRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.recordPayment(id, request, principal.id(), principal.role()));
    }

    @PostMapping("/payments/apply")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<ApplyFifoPaymentResponse> applyFifo(
        @Valid @RequestBody ApplyFifoPaymentRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(invoiceService.applyFifoPayment(request, principal.id(), principal.role()));
    }
}
