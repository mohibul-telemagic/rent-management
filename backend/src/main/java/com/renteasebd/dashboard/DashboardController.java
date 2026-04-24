package com.renteasebd.dashboard;

import com.renteasebd.common.ApiResponse;
import com.renteasebd.dashboard.dto.DashboardOverdueAgingResponse;
import com.renteasebd.dashboard.dto.DashboardPropertyBreakdownResponse;
import com.renteasebd.dashboard.dto.DashboardSummaryResponse;
import com.renteasebd.dashboard.dto.DashboardTrendPointResponse;
import com.renteasebd.security.UserPrincipal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<DashboardSummaryResponse> summary(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long propertyId,
        @RequestParam(required = false) String fromMonth,
        @RequestParam(required = false) String toMonth
    ) {
        return ApiResponse.ok(dashboardService.summary(principal.id(), principal.role(), propertyId, fromMonth, toMonth));
    }

    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<DashboardTrendPointResponse>> trend(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Integer months,
        @RequestParam(required = false) Long propertyId,
        @RequestParam(required = false) String fromMonth,
        @RequestParam(required = false) String toMonth
    ) {
        return ApiResponse.ok(dashboardService.trend(principal.id(), principal.role(), months, propertyId, fromMonth, toMonth));
    }

    @GetMapping("/property-breakdown")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<DashboardPropertyBreakdownResponse>> propertyBreakdown(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long propertyId,
        @RequestParam(required = false) String fromMonth,
        @RequestParam(required = false) String toMonth
    ) {
        return ApiResponse.ok(dashboardService.propertyBreakdown(principal.id(), principal.role(), propertyId, fromMonth, toMonth));
    }

    @GetMapping("/overdue-aging")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<DashboardOverdueAgingResponse> overdueAging(
        @AuthenticationPrincipal UserPrincipal principal,
        @RequestParam(required = false) Long propertyId,
        @RequestParam(required = false) String fromMonth,
        @RequestParam(required = false) String toMonth
    ) {
        return ApiResponse.ok(dashboardService.overdueAging(principal.id(), principal.role(), propertyId, fromMonth, toMonth));
    }
}
