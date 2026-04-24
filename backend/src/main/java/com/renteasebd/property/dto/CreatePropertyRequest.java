package com.renteasebd.property.dto;

import com.renteasebd.domain.property.BdDivision;
import com.renteasebd.domain.property.PropertyType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePropertyRequest(
    @NotBlank @Size(min = 3, max = 150) String propertyName,
    @NotBlank @Size(min = 5, max = 200) String addressLine1,
    @Size(max = 200) String addressLine2,
    @NotBlank @Size(max = 100) String thana,
    @NotBlank @Size(max = 100) String district,
    @NotNull BdDivision division,
    @NotNull PropertyType propertyType,
    @NotNull @Min(1) Integer totalUnits,
    @Size(max = 2000) String ownerNotes
) {
}
