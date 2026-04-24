package com.renteasebd.tenant.dto;

import com.renteasebd.domain.tenant.EmergencyRelation;
import com.renteasebd.domain.tenant.SecurityDepositStatus;
import com.renteasebd.domain.tenant.TenantStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TenantResponse(
    Long id,
    String fullName,
    String phonePrimary,
    String phoneSecondary,
    String nidNumberMasked,
    LocalDate dateOfBirth,
    String permanentAddress,
    String currentAddress,
    String emergencyContactName,
    String emergencyContactPhone,
    EmergencyRelation emergencyContactRelation,
    LocalDate leaseStartDate,
    LocalDate leaseEndDate,
    BigDecimal monthlyRentBdt,
    BigDecimal securityDepositBdt,
    SecurityDepositStatus securityDepositStatus,
    TenantStatus status,
    boolean hasNidImage,
    String notes,
    Long propertyUnitId
) {
}
