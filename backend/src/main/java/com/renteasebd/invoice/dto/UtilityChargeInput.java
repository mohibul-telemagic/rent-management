package com.renteasebd.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UtilityChargeInput(
    @NotBlank @Size(max = 100) String label,
    @NotNull @DecimalMin(value = "0.00") BigDecimal amountBdt
) {
}
