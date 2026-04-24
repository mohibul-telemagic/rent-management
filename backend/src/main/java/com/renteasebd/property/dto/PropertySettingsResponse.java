package com.renteasebd.property.dto;

import java.math.BigDecimal;

public record PropertySettingsResponse(
    Long id,
    Long propertyId,
    Integer invoiceDueDayOfMonth,
    BigDecimal lateFeeFlatBdt,
    Integer lateFeeGraceDays,
    BigDecimal taxPercent,
    String invoiceFooterText
) {
}
