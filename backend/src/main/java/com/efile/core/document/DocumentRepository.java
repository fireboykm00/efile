package com.efile.core.document;

import com.efile.core.casemanagement.Case;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    Page<Document> findByType(DocumentType type, Pageable pageable);

    Page<Document> findByCaseRefId(Long caseId, Pageable pageable);

    Optional<Document> findByReceiptNumber(String receiptNumber);

    List<Document> findByCaseRef(Case caseRef);

    List<Document> findByCaseRefId(Long caseRefId);

    @Query("SELECT d FROM Document d WHERE LOWER(d.type) LIKE LOWER(CONCAT('%', :type, '%'))")
    Page<Document> findByTypeContaining(@Param("type") String type, Pageable pageable);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.status = :status")
    long countByStatus(@Param("status") DocumentStatus status);

    List<Document> findByUploadedAtAfter(Instant date);

    List<Document> findByUploadedAtBetween(Instant startDate, Instant endDate);
}
