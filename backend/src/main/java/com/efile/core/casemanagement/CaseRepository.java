package com.efile.core.casemanagement;

import com.efile.core.user.User;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CaseRepository extends JpaRepository<Case, Long> {

    Page<Case> findByAssignedToId(Long assignedToId, Pageable pageable);

    Page<Case> findByCreatedById(Long createdById, Pageable pageable);

    List<Case> findByStatus(CaseStatus status);

    List<Case> findByCreatedByOrAssignedTo(User createdBy, User assignedTo);

    @Query("SELECT c FROM Case c WHERE c.assignedTo.id = :assignedToId AND c.status = :status")
    Page<Case> findByAssignedToIdAndStatus(@Param("assignedToId") Long assignedToId, @Param("status") CaseStatus status, Pageable pageable);

    @Query("SELECT c FROM Case c WHERE c.status = :status AND c.createdAt < :date")
    List<Case> findByStatusAndCreatedAtBefore(@Param("status") CaseStatus status, @Param("date") Instant date);

    @Query("SELECT c FROM Case c WHERE c.assignedTo.id = :assignedToId AND c.status = :status AND c.createdAt < :date")
    List<Case> findByAssignedToIdAndStatusAndCreatedAtBefore(@Param("assignedToId") Long assignedToId, @Param("status") CaseStatus status, @Param("date") Instant date);
}
