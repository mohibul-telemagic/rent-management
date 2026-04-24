package com.renteasebd.deposit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDepositTransactionRequest(
    @NotBlank @Size(max = 30) String txnType,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amountBdt,
    @Size(max = 300) String reason,
    @NotNull LocalDate transactionDate
) {
}
