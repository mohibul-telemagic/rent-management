package com.renteasebd.repository;

import com.renteasebd.domain.property.UtilityChargeConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilityChargeConfigRepository extends JpaRepository<UtilityChargeConfig, Long> {
    List<UtilityChargeConfig> findByPropertyIdOrderByDisplayOrderAscIdAsc(Long propertyId);

    Optional<UtilityChargeConfig> findByIdAndPropertyId(Long id, Long propertyId);
}
