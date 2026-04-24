package com.renteasebd.domain.invoice;

import jakarta.persistence.Column;
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
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "property_unit_id", nullable = false)
    private Long propertyUnitId;

    @Column(name = "billing_period_start", nullable = false)
    private LocalDate billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDate billingPeriodEnd;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "base_rent_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseRentBdt;

    @Column(name = "utility_charges_json")
    private String utilityChargesJson;

    @Column(name = "late_fee_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateFeeBdt = BigDecimal.ZERO;

    @Column(name = "tax_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxBdt = BigDecimal.ZERO;

    @Column(name = "total_due_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDueBdt;

    @Column(name = "balance_due_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceDueBdt;

    @Column(name = "sms_text")
    private String smsText;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getPropertyUnitId() {
        return propertyUnitId;
    }

    public void setPropertyUnitId(Long propertyUnitId) {
        this.propertyUnitId = propertyUnitId;
    }

    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public void setBillingPeriodStart(LocalDate billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public BigDecimal getBaseRentBdt() {
        return baseRentBdt;
    }

    public void setBaseRentBdt(BigDecimal baseRentBdt) {
        this.baseRentBdt = baseRentBdt;
    }

    public String getUtilityChargesJson() {
        return utilityChargesJson;
    }

    public void setUtilityChargesJson(String utilityChargesJson) {
        this.utilityChargesJson = utilityChargesJson;
    }

    public BigDecimal getLateFeeBdt() {
        return lateFeeBdt;
    }

    public void setLateFeeBdt(BigDecimal lateFeeBdt) {
        this.lateFeeBdt = lateFeeBdt;
    }

    public BigDecimal getTaxBdt() {
        return taxBdt;
    }

    public void setTaxBdt(BigDecimal taxBdt) {
        this.taxBdt = taxBdt;
    }

    public BigDecimal getTotalDueBdt() {
        return totalDueBdt;
    }

    public void setTotalDueBdt(BigDecimal totalDueBdt) {
        this.totalDueBdt = totalDueBdt;
    }

    public BigDecimal getBalanceDueBdt() {
        return balanceDueBdt;
    }

    public void setBalanceDueBdt(BigDecimal balanceDueBdt) {
        this.balanceDueBdt = balanceDueBdt;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
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
