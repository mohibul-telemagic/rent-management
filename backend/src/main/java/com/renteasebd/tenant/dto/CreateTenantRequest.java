package com.renteasebd.tenant.dto;

import com.renteasebd.domain.tenant.EmergencyRelation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class CreateTenantRequest {

    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+8801[3-9]\\d{8}$")
    private String phonePrimary;

    @Pattern(regexp = "^\\+8801[3-9]\\d{8}$")
    private String phoneSecondary;

    @NotBlank
    @Pattern(regexp = "^\\d{17}$")
    private String nidNumber;

    private MultipartFile nidImage;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(min = 10)
    private String permanentAddress;

    @Size(min = 10)
    private String currentAddress;

    @NotBlank
    @Size(min = 2, max = 120)
    private String emergencyContactName;

    @NotBlank
    @Pattern(regexp = "^\\+8801[3-9]\\d{8}$")
    private String emergencyContactPhone;

    @NotNull
    private EmergencyRelation emergencyContactRelation;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leaseStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate leaseEndDate;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monthlyRentBdt;

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal securityDepositBdt;

    @Size(max = 1000)
    private String notes;

    @NotNull
    private Long propertyUnitId;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhonePrimary() { return phonePrimary; }
    public void setPhonePrimary(String phonePrimary) { this.phonePrimary = phonePrimary; }
    public String getPhoneSecondary() { return phoneSecondary; }
    public void setPhoneSecondary(String phoneSecondary) { this.phoneSecondary = phoneSecondary; }
    public String getNidNumber() { return nidNumber; }
    public void setNidNumber(String nidNumber) { this.nidNumber = nidNumber; }
    public MultipartFile getNidImage() { return nidImage; }
    public void setNidImage(MultipartFile nidImage) { this.nidImage = nidImage; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }
    public EmergencyRelation getEmergencyContactRelation() { return emergencyContactRelation; }
    public void setEmergencyContactRelation(EmergencyRelation emergencyContactRelation) { this.emergencyContactRelation = emergencyContactRelation; }
    public LocalDate getLeaseStartDate() { return leaseStartDate; }
    public void setLeaseStartDate(LocalDate leaseStartDate) { this.leaseStartDate = leaseStartDate; }
    public LocalDate getLeaseEndDate() { return leaseEndDate; }
    public void setLeaseEndDate(LocalDate leaseEndDate) { this.leaseEndDate = leaseEndDate; }
    public BigDecimal getMonthlyRentBdt() { return monthlyRentBdt; }
    public void setMonthlyRentBdt(BigDecimal monthlyRentBdt) { this.monthlyRentBdt = monthlyRentBdt; }
    public BigDecimal getSecurityDepositBdt() { return securityDepositBdt; }
    public void setSecurityDepositBdt(BigDecimal securityDepositBdt) { this.securityDepositBdt = securityDepositBdt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getPropertyUnitId() { return propertyUnitId; }
    public void setPropertyUnitId(Long propertyUnitId) { this.propertyUnitId = propertyUnitId; }
}
