package com.renteasebd.invoice.dto;

import java.util.List;

public record BulkGenerateInvoicesResponse(
    int requestedCount,
    int generatedCount,
    int skippedCount,
    List<BulkGenerateInvoiceItem> items
) {
}
