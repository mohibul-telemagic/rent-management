package com.renteasebd.invoice;

import com.renteasebd.common.AppException;
import com.renteasebd.repository.InvoiceDownloadTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvoiceDownloadTokenService {

    private final InvoiceDownloadTokenRepository tokenRepository;
    private final InvoiceLinkProperties linkProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public InvoiceDownloadTokenService(InvoiceDownloadTokenRepository tokenRepository, InvoiceLinkProperties linkProperties) {
        this.tokenRepository = tokenRepository;
        this.linkProperties = linkProperties;
    }

    @Transactional
    public String issueOneTimeDownloadUrl(Long invoiceId, Long actorUserId) {
        String rawToken = generateRawToken();
        InvoiceDownloadToken token = new InvoiceDownloadToken();
        token.setInvoiceId(invoiceId);
        token.setTokenHash(hash(rawToken));
        token.setCreatedBy(actorUserId);
        token.setExpiresAt(LocalDateTime.now().plusDays(Math.max(1, linkProperties.getPublicLinkTtlDays())));
        tokenRepository.save(token);

        String base = linkProperties.getPublicBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/api/v1/public/invoices/download/" + rawToken;
    }

    @Transactional(readOnly = true)
    public Long resolveInvoiceIdForActiveToken(String rawToken) {
        InvoiceDownloadToken token = tokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new AppException("INVOICE_LINK_INVALID", "Invoice download link is invalid", "token"));

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new AppException("INVOICE_LINK_EXPIRED", "Invoice download link has expired", "token");
        }
        if (token.getConsumedAt() != null) {
            throw new AppException("INVOICE_LINK_ALREADY_USED", "Invoice download link has already been used", "token");
        }
        return token.getInvoiceId();
    }

    @Transactional
    public void consumeToken(String rawToken) {
        InvoiceDownloadToken token = tokenRepository.findByTokenHash(hash(rawToken))
            .orElseThrow(() -> new AppException("INVOICE_LINK_INVALID", "Invoice download link is invalid", "token"));

        LocalDateTime now = LocalDateTime.now();
        if (token.getExpiresAt().isBefore(now)) {
            throw new AppException("INVOICE_LINK_EXPIRED", "Invoice download link has expired", "token");
        }
        if (token.getConsumedAt() != null) {
            throw new AppException("INVOICE_LINK_ALREADY_USED", "Invoice download link has already been used", "token");
        }

        token.setConsumedAt(now);
        tokenRepository.save(token);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new AppException("INVOICE_LINK_HASH_FAILED", "Failed to process invoice link token", "token");
        }
    }
}
