package com.renteasebd.repository;

import com.renteasebd.invoice.InvoiceDownloadToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceDownloadTokenRepository extends JpaRepository<InvoiceDownloadToken, Long> {

    Optional<InvoiceDownloadToken> findByTokenHash(String tokenHash);
}
