package com.renteasebd.domain.tenant;

import com.renteasebd.security.AesGcmStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Convert(converter = AesGcmStringConverter.class)
    @Column(name = "phone_primary", nullable = false)
    private String phonePrimary;

    @Convert(converter = AesGcmStringConverter.class)
    @Column(name = "phone_secondary")
    private String phoneSecondary;

    @Convert(converter = AesGcmStringConverter.class)
    @Column(name = "nid_number", nullable = false)
    private String nidNumber;

    @Column(name = "nid_image")
    private byte[] nidImage;

    @Column(name = "nid_image_mime_type", length = 20)
    private String nidImageMimeType;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "permanent_address", nullable = false)
    private String permanentAddress;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "emergency_contact_name", nullable = false, length = 120)
    private String emergencyContactName;

    @Convert(converter = AesGcmStringConverter.class)
    @Column(name = "emergency_contact_phone", nullable = false)
    private String emergencyContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_contact_relation", nullable = false, length = 50)
    private EmergencyRelation emergencyContactRelation;

    @Column(name = "lease_start_date", nullable = false)
    private LocalDate leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDate leaseEndDate;

    @Column(name = "monthly_rent_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRentBdt;

    @Column(name = "security_deposit_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal securityDepositBdt;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_deposit_status", nullable = false, length = 30)
    private SecurityDepositStatus securityDepositStatus = SecurityDepositStatus.HELD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "notes")
    private String notes;

    @Column(name = "property_unit_id", nullable = false)
    private Long propertyUnitId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhonePrimary() {
        return phonePrimary;
    }

    public void setPhonePrimary(String phonePrimary) {
        this.phonePrimary = phonePrimary;
    }

    public String getPhoneSecondary() {
        return phoneSecondary;
    }

    public void setPhoneSecondary(String phoneSecondary) {
        this.phoneSecondary = phoneSecondary;
    }

    public String getNidNumber() {
        return nidNumber;
    }

    public void setNidNumber(String nidNumber) {
        this.nidNumber = nidNumber;
    }

    public byte[] getNidImage() {
        return nidImage;
    }

    public void setNidImage(byte[] nidImage) {
        this.nidImage = nidImage;
    }

    public String getNidImageMimeType() {
        return nidImageMimeType;
    }

    public void setNidImageMimeType(String nidImageMimeType) {
        this.nidImageMimeType = nidImageMimeType;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactPhone() {
        return emergencyContactPhone;
    }

    public void setEmergencyContactPhone(String emergencyContactPhone) {
        this.emergencyContactPhone = emergencyContactPhone;
    }

    public EmergencyRelation getEmergencyContactRelation() {
        return emergencyContactRelation;
    }

    public void setEmergencyContactRelation(EmergencyRelation emergencyContactRelation) {
        this.emergencyContactRelation = emergencyContactRelation;
    }

    public LocalDate getLeaseStartDate() {
        return leaseStartDate;
    }

    public void setLeaseStartDate(LocalDate leaseStartDate) {
        this.leaseStartDate = leaseStartDate;
    }

    public LocalDate getLeaseEndDate() {
        return leaseEndDate;
    }

    public void setLeaseEndDate(LocalDate leaseEndDate) {
        this.leaseEndDate = leaseEndDate;
    }

    public BigDecimal getMonthlyRentBdt() {
        return monthlyRentBdt;
    }

    public void setMonthlyRentBdt(BigDecimal monthlyRentBdt) {
        this.monthlyRentBdt = monthlyRentBdt;
    }

    public BigDecimal getSecurityDepositBdt() {
        return securityDepositBdt;
    }

    public void setSecurityDepositBdt(BigDecimal securityDepositBdt) {
        this.securityDepositBdt = securityDepositBdt;
    }

    public SecurityDepositStatus getSecurityDepositStatus() {
        return securityDepositStatus;
    }

    public void setSecurityDepositStatus(SecurityDepositStatus securityDepositStatus) {
        this.securityDepositStatus = securityDepositStatus;
    }

    public TenantStatus getStatus() {
        return status;
    }

    public void setStatus(TenantStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getPropertyUnitId() {
        return propertyUnitId;
    }

    public void setPropertyUnitId(Long propertyUnitId) {
        this.propertyUnitId = propertyUnitId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
