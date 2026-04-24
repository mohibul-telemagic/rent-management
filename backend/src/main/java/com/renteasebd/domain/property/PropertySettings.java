package com.renteasebd.domain.property;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_settings")
public class PropertySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false, unique = true)
    private Long propertyId;

    @Column(name = "invoice_due_day_of_month", nullable = false)
    private Integer invoiceDueDayOfMonth = 5;

    @Column(name = "late_fee_flat_bdt", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateFeeFlatBdt = BigDecimal.ZERO;

    @Column(name = "late_fee_grace_days", nullable = false)
    private Integer lateFeeGraceDays = 0;

    @Column(name = "tax_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "invoice_footer_text", length = 500)
    private String invoiceFooterText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public Integer getInvoiceDueDayOfMonth() {
        return invoiceDueDayOfMonth;
    }

    public void setInvoiceDueDayOfMonth(Integer invoiceDueDayOfMonth) {
        this.invoiceDueDayOfMonth = invoiceDueDayOfMonth;
    }

    public BigDecimal getLateFeeFlatBdt() {
        return lateFeeFlatBdt;
    }

    public void setLateFeeFlatBdt(BigDecimal lateFeeFlatBdt) {
        this.lateFeeFlatBdt = lateFeeFlatBdt;
    }

    public Integer getLateFeeGraceDays() {
        return lateFeeGraceDays;
    }

    public void setLateFeeGraceDays(Integer lateFeeGraceDays) {
        this.lateFeeGraceDays = lateFeeGraceDays;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public String getInvoiceFooterText() {
        return invoiceFooterText;
    }

    public void setInvoiceFooterText(String invoiceFooterText) {
        this.invoiceFooterText = invoiceFooterText;
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
