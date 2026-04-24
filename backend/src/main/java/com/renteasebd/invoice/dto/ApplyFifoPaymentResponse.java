package com.renteasebd.invoice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ApplyFifoPaymentResponse(
    Long tenantId,
    BigDecimal requestedAmountBdt,
    BigDecimal appliedAmountBdt,
    BigDecimal remainingUnappliedBdt,
    List<PaymentAllocationItem> allocations
) {
}
