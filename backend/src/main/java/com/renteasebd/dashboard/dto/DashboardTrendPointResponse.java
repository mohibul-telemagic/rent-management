package com.renteasebd.dashboard.dto;

import java.math.BigDecimal;

public record DashboardTrendPointResponse(
    String month,
    int invoiceCount,
    BigDecimal totalDueBdt,
    BigDecimal collectedBdt,
    BigDecimal outstandingBdt
) {
}
