package com.renteasebd.invoice.dto;

import java.util.List;

public record BulkGenerateInvoiceItem(
    Long tenantId,
    String tenantName,
    String status,
    Long invoiceId,
    String reason,
    List<String> warnings
) {
}
