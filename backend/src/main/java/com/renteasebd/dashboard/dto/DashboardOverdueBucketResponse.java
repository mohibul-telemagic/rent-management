package com.renteasebd.dashboard.dto;

import java.math.BigDecimal;

public record DashboardOverdueBucketResponse(
    String bucket,
    int invoiceCount,
    BigDecimal amountBdt
) {
}
