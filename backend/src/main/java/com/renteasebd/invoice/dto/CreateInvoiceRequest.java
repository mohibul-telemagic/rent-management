package com.renteasebd.invoice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record CreateInvoiceRequest(
    @NotNull Long tenantId,
    @NotNull LocalDate billingPeriodStart,
    @Valid List<UtilityChargeInput> utilityCharges
) {
}
