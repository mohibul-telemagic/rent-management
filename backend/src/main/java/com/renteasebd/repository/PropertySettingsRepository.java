package com.renteasebd.repository;

import com.renteasebd.domain.property.PropertySettings;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertySettingsRepository extends JpaRepository<PropertySettings, Long> {
    Optional<PropertySettings> findByPropertyId(Long propertyId);
}
