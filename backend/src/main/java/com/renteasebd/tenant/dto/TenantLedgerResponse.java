package com.renteasebd.tenant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TenantLedgerResponse(
    Long tenantId,
    BigDecimal totalInvoicedBdt,
    BigDecimal outstandingBdt,
    BigDecimal totalPaidBdt,
    BigDecimal totalDepositNetBdt,
    List<InvoiceLedgerItem> invoices,
    List<PaymentLedgerItem> payments,
    List<DepositLedgerItem> deposits
) {
    public record InvoiceLedgerItem(
        Long invoiceId,
        LocalDate billingPeriodStart,
        LocalDate billingPeriodEnd,
        LocalDate dueDate,
        String status,
        BigDecimal totalDueBdt,
        BigDecimal balanceDueBdt
    ) {
    }

    public record PaymentLedgerItem(
        Long paymentId,
        Long invoiceId,
        LocalDate paymentDate,
        BigDecimal amountBdt,
        String paymentMethod,
        String notes
    ) {
    }

    public record DepositLedgerItem(
        Long transactionId,
        LocalDate transactionDate,
        String txnType,
        BigDecimal amountBdt,
        String reason
    ) {
    }
}
