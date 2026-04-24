package com.renteasebd.property;

import com.renteasebd.audit.dto.AuditTimelineItemResponse;
import com.renteasebd.common.ApiResponse;
import com.renteasebd.property.dto.CreatePropertyRequest;
import com.renteasebd.property.dto.PropertyResponse;
import com.renteasebd.property.dto.PropertySettingsResponse;
import com.renteasebd.property.dto.UnitResponse;
import com.renteasebd.property.dto.UpdatePropertyRequest;
import com.renteasebd.property.dto.UpsertPropertySettingsRequest;
import com.renteasebd.property.dto.UpsertUnitRequest;
import com.renteasebd.property.dto.UpsertUtilityChargeConfigRequest;
import com.renteasebd.property.dto.UtilityChargeConfigResponse;
import com.renteasebd.security.UserPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<PropertyResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(propertyService.list(principal.id(), principal.role()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<PropertyResponse> get(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(propertyService.get(id, principal.id(), principal.role()));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<PropertyResponse> create(
        @Valid @RequestBody CreatePropertyRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.create(request, principal.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<PropertyResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdatePropertyRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.update(id, request, principal.id(), principal.role()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        propertyService.softDelete(id, principal.id(), principal.role());
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/units")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<UnitResponse>> listUnits(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(propertyService.listUnits(id, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/units")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<UnitResponse> addUnit(
        @PathVariable Long id,
        @Valid @RequestBody UpsertUnitRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.addUnit(id, request, principal.id(), principal.role()));
    }

    @PutMapping("/{id}/units/{unitId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<UnitResponse> updateUnit(
        @PathVariable Long id,
        @PathVariable Long unitId,
        @Valid @RequestBody UpsertUnitRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.updateUnit(id, unitId, request, principal.id(), principal.role()));
    }

    @GetMapping("/{id}/settings")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<PropertySettingsResponse> getSettings(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.getSettings(id, principal.id(), principal.role()));
    }

    @PutMapping("/{id}/settings")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<PropertySettingsResponse> upsertSettings(
        @PathVariable Long id,
        @Valid @RequestBody UpsertPropertySettingsRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.upsertSettings(id, request, principal.id(), principal.role()));
    }

    @GetMapping("/{id}/utility-charge-configs")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<UtilityChargeConfigResponse>> listUtilityChargeConfigs(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.listUtilityChargeConfigs(id, principal.id(), principal.role()));
    }

    @GetMapping("/{id}/activity")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
    public ApiResponse<List<AuditTimelineItemResponse>> activity(
        @PathVariable Long id,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.activity(id, principal.id(), principal.role()));
    }

    @PostMapping("/{id}/utility-charge-configs")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<UtilityChargeConfigResponse> createUtilityChargeConfig(
        @PathVariable Long id,
        @Valid @RequestBody UpsertUtilityChargeConfigRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(propertyService.createUtilityChargeConfig(id, request, principal.id(), principal.role()));
    }

    @PutMapping("/{id}/utility-charge-configs/{configId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<UtilityChargeConfigResponse> updateUtilityChargeConfig(
        @PathVariable Long id,
        @PathVariable Long configId,
        @Valid @RequestBody UpsertUtilityChargeConfigRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(
            propertyService.updateUtilityChargeConfig(id, configId, request, principal.id(), principal.role()));
    }

    @DeleteMapping("/{id}/utility-charge-configs/{configId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<Void> deleteUtilityChargeConfig(
        @PathVariable Long id,
        @PathVariable Long configId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        propertyService.deleteUtilityChargeConfig(id, configId, principal.id(), principal.role());
        return ApiResponse.ok(null);
    }
}
