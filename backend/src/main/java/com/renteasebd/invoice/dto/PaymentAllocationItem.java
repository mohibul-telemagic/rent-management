package com.renteasebd.invoice.dto;

import com.renteasebd.domain.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentAllocationItem(
    Long invoiceId,
    LocalDate billingPeriodStart,
    BigDecimal appliedAmountBdt,
    BigDecimal remainingBalanceBdt,
    InvoiceStatus newStatus
) {
}
