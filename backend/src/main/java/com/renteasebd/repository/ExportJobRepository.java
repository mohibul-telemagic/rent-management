package com.renteasebd.repository;

import com.renteasebd.domain.export.ExportJob;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportJobRepository extends JpaRepository<ExportJob, String> {
    Optional<ExportJob> findByIdAndOwnerId(String id, Long ownerId);
}
