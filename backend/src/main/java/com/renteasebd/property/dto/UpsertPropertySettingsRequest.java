package com.renteasebd.property.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpsertPropertySettingsRequest(
    @NotNull @Min(1) @Max(31) Integer invoiceDueDayOfMonth,
    @NotNull @DecimalMin(value = "0.00") BigDecimal lateFeeFlatBdt,
    @NotNull @Min(0) Integer lateFeeGraceDays,
    @NotNull @DecimalMin(value = "0.00") @DecimalMax(value = "100.00") BigDecimal taxPercent,
    @Size(max = 500) String invoiceFooterText
) {
}
