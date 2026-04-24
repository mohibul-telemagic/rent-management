package com.renteasebd.invoice.dto;

import java.math.BigDecimal;

public record RecordInvoicePaymentResponse(
    Long invoiceId,
    BigDecimal appliedAmountBdt,
    InvoiceResponse invoice
) {
}
