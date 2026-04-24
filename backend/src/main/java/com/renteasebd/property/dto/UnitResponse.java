package com.renteasebd.property.dto;

import com.renteasebd.domain.property.OccupancyStatus;
import java.math.BigDecimal;

public record UnitResponse(
    Long id,
    Long propertyId,
    String unitIdentifier,
    Integer floorNumber,
    BigDecimal areaSqft,
    String unitType,
    OccupancyStatus occupancyStatus
) {
}
