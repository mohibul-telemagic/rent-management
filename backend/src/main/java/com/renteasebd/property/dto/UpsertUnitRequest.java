package com.renteasebd.property.dto;

import com.renteasebd.domain.property.OccupancyStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpsertUnitRequest(
    @NotBlank @Size(max = 20) String unitIdentifier,
    Integer floorNumber,
    @DecimalMin(value = "0.0", inclusive = false) BigDecimal areaSqft,
    @Size(max = 20) String unitType,
    @NotNull OccupancyStatus occupancyStatus
) {
}
