package com.renteasebd.property.dto;

public record UtilityChargeConfigResponse(
    Long id,
    Long propertyId,
    String label,
    boolean isEnabled,
    Integer displayOrder
) {
}
