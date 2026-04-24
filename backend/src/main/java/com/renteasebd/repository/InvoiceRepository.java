package com.renteasebd.repository;

import com.renteasebd.domain.invoice.Invoice;
import com.renteasebd.domain.invoice.InvoiceStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByTenantIdAndBillingPeriodStartAndStatusNotIn(
        Long tenantId,
        LocalDate billingPeriodStart,
        Collection<InvoiceStatus> statuses
    );

    List<Invoice> findByTenantIdAndStatusInOrderByBillingPeriodStartAscIdAsc(Long tenantId, Collection<InvoiceStatus> statuses);
    List<Invoice> findByTenantIdOrderByBillingPeriodStartDescIdDesc(Long tenantId);

    List<Invoice> findByPropertyUnitIdInOrderByBillingPeriodStartDescIdDesc(List<Long> propertyUnitIds);
}
