package com.renteasebd.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    Long propertyId,
    String propertyName,
    String category,
    BigDecimal amountBdt,
    LocalDate expenseDate,
    String description,
    boolean hasReceipt,
    String receiptMimeType
) {
}
