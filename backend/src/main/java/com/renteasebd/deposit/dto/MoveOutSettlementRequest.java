package com.renteasebd.deposit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MoveOutSettlementRequest(
    @NotNull LocalDate moveOutDate,
    @NotNull @DecimalMin(value = "0.0") BigDecimal deductionBdt,
    @NotNull @DecimalMin(value = "0.0") BigDecimal refundBdt,
    @Size(max = 300) String reason
) {
}
