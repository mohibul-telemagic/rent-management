package com.renteasebd.property.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertUtilityChargeConfigRequest(
    @NotBlank @Size(max = 100) String label,
    @NotNull Boolean isEnabled,
    @NotNull @Min(0) Integer displayOrder
) {
}
