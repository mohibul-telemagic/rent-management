package com.renteasebd.repository;

import com.renteasebd.domain.invoice.InvoicePayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, Long> {
    List<InvoicePayment> findByInvoiceIdOrderByPaymentDateDescIdDesc(Long invoiceId);
    List<InvoicePayment> findByInvoiceIdInOrderByPaymentDateDescIdDesc(List<Long> invoiceIds);
}
