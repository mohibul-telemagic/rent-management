package com.renteasebd.repository;

import com.renteasebd.domain.tenant.Tenant;
import com.renteasebd.domain.tenant.TenantStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByPropertyUnitIdIn(List<Long> propertyUnitIds);
    List<Tenant> findByPropertyUnitIdInAndStatus(List<Long> propertyUnitIds, TenantStatus status);
    List<Tenant> findByPropertyUnitId(Long propertyUnitId);
    List<Tenant> findByStatus(TenantStatus status);
    boolean existsByPropertyUnitIdAndStatus(Long propertyUnitId, TenantStatus status);
}
