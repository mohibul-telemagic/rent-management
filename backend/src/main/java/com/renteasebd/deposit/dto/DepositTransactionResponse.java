package com.renteasebd.deposit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositTransactionResponse(
    Long id,
    Long tenantId,
    String tenantName,
    Long propertyUnitId,
    String unitIdentifier,
    Long propertyId,
    String propertyName,
    String txnType,
    BigDecimal amountBdt,
    String reason,
    LocalDate transactionDate,
    boolean refundReceiptAvailable
) {
}
