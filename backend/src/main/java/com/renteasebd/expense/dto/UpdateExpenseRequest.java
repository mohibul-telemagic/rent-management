package com.renteasebd.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateExpenseRequest(
    @NotNull Long propertyId,
    @NotBlank @Size(max = 50) String category,
    @NotNull @DecimalMin(value = "0.01") BigDecimal amountBdt,
    @NotNull LocalDate expenseDate,
    @Size(max = 500) String description
) {
}
