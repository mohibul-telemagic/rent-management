package com.renteasebd.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardOverdueAgingResponse(
    List<DashboardOverdueBucketResponse> buckets,
    int overdueInvoiceCount,
    BigDecimal overdueAmountBdt
) {
}
