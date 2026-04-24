package com.renteasebd.invoice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.invoice")
public class InvoiceLinkProperties {

    private String publicBaseUrl = "http://localhost:8080";
    private int publicLinkTtlDays = 7;

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }

    public void setPublicBaseUrl(String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public int getPublicLinkTtlDays() {
        return publicLinkTtlDays;
    }

    public void setPublicLinkTtlDays(int publicLinkTtlDays) {
        this.publicLinkTtlDays = publicLinkTtlDays;
    }
}
