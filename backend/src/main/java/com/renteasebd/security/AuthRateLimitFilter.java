package com.renteasebd.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renteasebd.common.ApiResponse;
import com.renteasebd.config.AppSecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
        "/api/v1/auth/login",
        "/api/v1/auth/refresh",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password"
    );

    private final AppSecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    private final Map<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(AppSecurityProperties securityProperties, ObjectMapper objectMapper) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return !LIMITED_PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String ip = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        String path = request.getRequestURI();
        String key = ip + "|" + path;
        long nowEpochSeconds = Instant.now().getEpochSecond();
        int maxAttempts = Math.max(1, securityProperties.getAuthRateLimitMaxAttempts());
        int windowSeconds = Math.max(1, securityProperties.getAuthRateLimitWindowSeconds());

        SlidingWindowCounter counter = counters.computeIfAbsent(key, ignored -> new SlidingWindowCounter(nowEpochSeconds));
        long windowStart = counter.windowStartEpochSeconds;
        if (nowEpochSeconds - windowStart >= windowSeconds) {
            counter.windowStartEpochSeconds = nowEpochSeconds;
            counter.attempts.set(0);
        }

        int attempts = counter.attempts.incrementAndGet();
        if (attempts > maxAttempts) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail("RATE_LIMITED", "Too many auth requests. Try again shortly.", null)
            ));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class SlidingWindowCounter {
        private volatile long windowStartEpochSeconds;
        private final AtomicInteger attempts = new AtomicInteger(0);

        private SlidingWindowCounter(long windowStartEpochSeconds) {
            this.windowStartEpochSeconds = windowStartEpochSeconds;
        }
    }
}
