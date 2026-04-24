package com.renteasebd.notification;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.common.AppException;
import com.renteasebd.notification.dto.NotificationEventResponse;
import com.renteasebd.security.UserPrincipal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationEventResponse>> list(
        @RequestParam(required = false) Long tenantId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        return ApiResponse.ok(notificationService.list(principal.id(), principal.role(), tenantId));
    }

    @PostMapping("/overdue-reminders/run")
    public ApiResponse<Map<String, Integer>> runOverdueReminders(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AppException("UNAUTHORIZED", "Authentication required", null);
        }
        int created = notificationService.createOverdueReminders(principal.id(), principal.role());
        return ApiResponse.ok(Map.of("createdCount", created));
    }
}
