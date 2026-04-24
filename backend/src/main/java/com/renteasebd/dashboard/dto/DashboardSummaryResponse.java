package com.renteasebd.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryResponse(
    int totalProperties,
    int totalUnits,
    int occupiedUnits,
    int activeTenants,
    BigDecimal occupancyRatePercent,
    BigDecimal receivableBdt,
    int overdueInvoiceCount
) {
}
