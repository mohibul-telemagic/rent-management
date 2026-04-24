package com.renteasebd.invoice;

import com.renteasebd.export.PdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/invoices")
public class PublicInvoiceController {

    private final InvoiceDownloadTokenService tokenService;
    private final PdfService pdfService;

    public PublicInvoiceController(InvoiceDownloadTokenService tokenService, PdfService pdfService) {
        this.tokenService = tokenService;
        this.pdfService = pdfService;
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<byte[]> downloadWithToken(
        @PathVariable String token,
        @RequestParam(defaultValue = "en") String lang
    ) {
        Long invoiceId = tokenService.resolveInvoiceIdForActiveToken(token);
        PdfService.GeneratedPdf pdf = pdfService.generateInvoicePdfPublic(invoiceId, lang);
        tokenService.consumeToken(token);

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=invoice-" + invoiceId + ".pdf")
            .contentType(MediaType.APPLICATION_PDF);
        if (pdf.fallbackToEnglish()) {
            builder.header("X-Language-Fallback", "en");
        }
        return builder.body(pdf.bytes());
    }
}
