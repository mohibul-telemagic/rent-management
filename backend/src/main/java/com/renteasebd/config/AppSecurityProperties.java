package com.renteasebd.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private List<String> corsAllowedOrigins = List.of(
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:5174",
        "http://127.0.0.1:5174",
        "http://localhost:5175",
        "http://127.0.0.1:5175"
    );
    private int authRateLimitMaxAttempts = 20;
    private int authRateLimitWindowSeconds = 60;

    public List<String> getCorsAllowedOrigins() {
        return corsAllowedOrigins;
    }

    public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
        this.corsAllowedOrigins = corsAllowedOrigins;
    }

    public int getAuthRateLimitMaxAttempts() {
        return authRateLimitMaxAttempts;
    }

    public void setAuthRateLimitMaxAttempts(int authRateLimitMaxAttempts) {
        this.authRateLimitMaxAttempts = authRateLimitMaxAttempts;
    }

    public int getAuthRateLimitWindowSeconds() {
        return authRateLimitWindowSeconds;
    }

    public void setAuthRateLimitWindowSeconds(int authRateLimitWindowSeconds) {
        this.authRateLimitWindowSeconds = authRateLimitWindowSeconds;
    }
}
