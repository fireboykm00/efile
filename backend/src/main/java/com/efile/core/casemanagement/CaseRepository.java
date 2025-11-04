package com.efile.core.casemanagement;

import com.efile.core.user.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<Case, Long> {

    Page<Case> findByAssignedToId(Long assignedToId, Pageable pageable);

    Page<Case> findByCreatedById(Long createdById, Pageable pageable);

    List<Case> findByStatus(CaseStatus status);

    List<Case> findByCreatedByOrAssignedTo(User createdBy, User assignedTo);
}
