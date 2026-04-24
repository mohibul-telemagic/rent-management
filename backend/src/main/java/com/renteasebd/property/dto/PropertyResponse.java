package com.renteasebd.property.dto;

import com.renteasebd.domain.property.BdDivision;
import com.renteasebd.domain.property.PropertyType;

public record PropertyResponse(
    Long id,
    String propertyName,
    String addressLine1,
    String addressLine2,
    String thana,
    String district,
    BdDivision division,
    PropertyType propertyType,
    Integer totalUnits,
    String ownerNotes,
    String status
) {
}
