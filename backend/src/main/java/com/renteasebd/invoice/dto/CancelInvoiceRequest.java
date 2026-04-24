package com.renteasebd.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelInvoiceRequest(
    @NotBlank @Size(max = 300) String reason
) {
}
