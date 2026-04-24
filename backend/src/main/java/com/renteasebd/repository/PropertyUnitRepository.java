package com.renteasebd.repository;

import com.renteasebd.domain.property.PropertyUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyUnitRepository extends JpaRepository<PropertyUnit, Long> {
    List<PropertyUnit> findByPropertyId(Long propertyId);
    List<PropertyUnit> findByPropertyIdIn(List<Long> propertyIds);
    long countByPropertyId(Long propertyId);
    Optional<PropertyUnit> findByIdAndPropertyId(Long id, Long propertyId);
}
