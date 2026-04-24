package com.renteasebd.auth;

import com.renteasebd.auth.dto.AuthResponse;
import com.renteasebd.auth.dto.AuthSessionResponse;
import com.renteasebd.auth.dto.ForgotPasswordRequest;
import com.renteasebd.auth.dto.LoginRequest;
import com.renteasebd.auth.dto.LogoutRequest;
import com.renteasebd.auth.dto.RefreshRequest;
import com.renteasebd.auth.dto.ResetPasswordRequest;
import com.renteasebd.auth.dto.UpdatePreferredLanguageRequest;
import com.renteasebd.common.ApiResponse;
import com.renteasebd.common.AppException;
import com.renteasebd.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String userAgent = servletRequest.getHeader("User-Agent");
        return ApiResponse.ok(authService.login(request, userAgent));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    @PatchMapping("/preferred-language")
    public ApiResponse<Void> updatePreferredLanguage(
        @Valid @RequestBody UpdatePreferredLanguageRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        authService.updatePreferredLanguage(principal.id(), request.language());
        return ApiResponse.ok(null);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @Valid @RequestBody LogoutRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long actorUserId = principal == null ? null : principal.id();
        authService.logout(request.refreshToken(), actorUserId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/logout-all")
    public ApiResponse<Void> logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        authService.logoutAll(principal.id());
        return ApiResponse.ok(null);
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AuthSessionResponse>> listSessions(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        return ApiResponse.ok(authService.listSessions(principal.id()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> revokeSession(
        @PathVariable String sessionId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        authService.revokeSession(principal.id(), sessionId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok(null);
    }
}
