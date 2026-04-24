package com.renteasebd.invoice.dto;

import java.util.List;

public record GenerateInvoiceResponse(
    InvoiceResponse invoice,
    List<String> warnings
) {
}
