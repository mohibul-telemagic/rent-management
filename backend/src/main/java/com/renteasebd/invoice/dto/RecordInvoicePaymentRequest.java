package com.renteasebd.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordInvoicePaymentRequest(
    @NotNull @DecimalMin(value = "0.01") BigDecimal amountBdt,
    @NotNull LocalDate paymentDate,
    @NotBlank @Size(max = 30) String paymentMethod,
    @Size(max = 500) String notes
) {
}
