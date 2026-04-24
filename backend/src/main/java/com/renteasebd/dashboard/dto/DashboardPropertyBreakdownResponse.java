package com.renteasebd.dashboard.dto;

import java.math.BigDecimal;

public record DashboardPropertyBreakdownResponse(
    Long propertyId,
    String propertyName,
    int totalUnits,
    int occupiedUnits,
    int activeTenants,
    BigDecimal receivableBdt,
    int overdueInvoiceCount
) {
}
