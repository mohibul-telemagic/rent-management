package com.renteasebd.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String privateKeyPem;
    private String publicKeyPem;
    private Duration accessTokenTtl = Duration.ofHours(24);
    private Duration refreshTokenTtl = Duration.ofDays(30);
    private boolean allowEphemeralKeyPair = true;

    public String getPrivateKeyPem() {
        return privateKeyPem;
    }

    public void setPrivateKeyPem(String privateKeyPem) {
        this.privateKeyPem = privateKeyPem;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public void setPublicKeyPem(String publicKeyPem) {
        this.publicKeyPem = publicKeyPem;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public boolean isAllowEphemeralKeyPair() {
        return allowEphemeralKeyPair;
    }

    public void setAllowEphemeralKeyPair(boolean allowEphemeralKeyPair) {
        this.allowEphemeralKeyPair = allowEphemeralKeyPair;
    }
}
