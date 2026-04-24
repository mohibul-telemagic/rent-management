package com.renteasebd.deposit.dto;

import com.renteasebd.domain.tenant.SecurityDepositStatus;
import com.renteasebd.domain.tenant.TenantStatus;
import java.math.BigDecimal;

public record MoveOutSettlementResponse(
    Long tenantId,
    Long deductionTransactionId,
    Long refundTransactionId,
    BigDecimal currentBalanceBdt,
    TenantStatus tenantStatus,
    SecurityDepositStatus securityDepositStatus
) {
}
