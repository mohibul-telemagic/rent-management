package com.renteasebd.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

public class CreateExpenseRequest {

    @NotNull
    private Long propertyId;

    @NotBlank
    @Size(max = 50)
    private String category;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amountBdt;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expenseDate;

    @Size(max = 500)
    private String description;

    private MultipartFile receipt;

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmountBdt() {
        return amountBdt;
    }

    public void setAmountBdt(BigDecimal amountBdt) {
        this.amountBdt = amountBdt;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getReceipt() {
        return receipt;
    }

    public void setReceipt(MultipartFile receipt) {
        this.receipt = receipt;
    }
}
