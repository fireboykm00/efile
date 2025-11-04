package com.efile.core.audit;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);

    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenAndUserId(Instant start, Instant end, Long userId, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenAndAction(Instant start, Instant end, String action, Pageable pageable);

    Page<AuditLog> findByTimestampBetweenAndUserIdAndAction(Instant start, Instant end, Long userId, String action, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
}
