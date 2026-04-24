package com.renteasebd.deposit.dto;

import com.renteasebd.domain.tenant.SecurityDepositStatus;
import java.math.BigDecimal;

public record DepositBalanceResponse(
    Long tenantId,
    BigDecimal openingDepositBdt,
    BigDecimal collectedBdt,
    BigDecimal deductedBdt,
    BigDecimal refundedBdt,
    BigDecimal currentBalanceBdt,
    SecurityDepositStatus securityDepositStatus
) {
}
