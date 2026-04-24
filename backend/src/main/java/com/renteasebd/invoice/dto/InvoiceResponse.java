package com.renteasebd.invoice.dto;

import com.renteasebd.domain.invoice.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceResponse(
    Long id,
    Long tenantId,
    String tenantName,
    Long propertyUnitId,
    String unitIdentifier,
    Long propertyId,
    String propertyName,
    LocalDate billingPeriodStart,
    LocalDate billingPeriodEnd,
    LocalDate dueDate,
    InvoiceStatus status,
    BigDecimal baseRentBdt,
    BigDecimal lateFeeBdt,
    BigDecimal taxBdt,
    BigDecimal totalDueBdt,
    BigDecimal balanceDueBdt,
    String smsText,
    String invoiceDownloadUrl,
    String utilityChargesJson
) {
}
