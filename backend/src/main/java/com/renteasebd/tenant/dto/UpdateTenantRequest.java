package com.renteasebd.tenant.dto;

import com.renteasebd.domain.tenant.EmergencyRelation;
import com.renteasebd.domain.tenant.TenantStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateTenantRequest(
    @NotBlank @Size(min = 2, max = 120) String fullName,
    @NotBlank @Pattern(regexp = "^\\+8801[3-9]\\d{8}$") String phonePrimary,
    @Pattern(regexp = "^\\+8801[3-9]\\d{8}$") String phoneSecondary,
    @NotBlank @Pattern(regexp = "^\\d{17}$") String nidNumber,
    LocalDate dateOfBirth,
    @NotBlank @Size(min = 10) String permanentAddress,
    @Size(min = 10) String currentAddress,
    @NotBlank @Size(min = 2, max = 120) String emergencyContactName,
    @NotBlank @Pattern(regexp = "^\\+8801[3-9]\\d{8}$") String emergencyContactPhone,
    @NotNull EmergencyRelation emergencyContactRelation,
    @NotNull LocalDate leaseStartDate,
    LocalDate leaseEndDate,
    @NotNull @DecimalMin(value = "0.01") BigDecimal monthlyRentBdt,
    @NotNull @DecimalMin(value = "0.0") BigDecimal securityDepositBdt,
    @NotNull TenantStatus status,
    @Size(max = 1000) String notes,
    @NotNull Long propertyUnitId
) {
}
