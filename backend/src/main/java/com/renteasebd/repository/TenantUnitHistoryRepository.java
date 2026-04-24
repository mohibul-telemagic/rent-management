package com.renteasebd.repository;

import com.renteasebd.domain.tenant.TenantUnitHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantUnitHistoryRepository extends JpaRepository<TenantUnitHistory, Long> {
    List<TenantUnitHistory> findByTenantIdOrderByStartDateDesc(Long tenantId);
    Optional<TenantUnitHistory> findTopByTenantIdAndEndDateIsNullOrderByStartDateDesc(Long tenantId);
}
