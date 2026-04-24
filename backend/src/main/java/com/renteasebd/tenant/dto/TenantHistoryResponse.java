package com.renteasebd.tenant.dto;

import java.time.LocalDate;

public record TenantHistoryResponse(
    Long id,
    Long tenantId,
    Long propertyUnitId,
    LocalDate startDate,
    LocalDate endDate
) {
}
