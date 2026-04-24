package com.renteasebd.repository;

import com.renteasebd.domain.notification.NotificationEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findByOwnerUserIdOrderByCreatedAtDesc(Long ownerUserId);
    List<NotificationEvent> findByOwnerUserIdAndTenantIdOrderByCreatedAtDesc(Long ownerUserId, Long tenantId);
}
