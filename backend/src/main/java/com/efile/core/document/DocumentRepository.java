package com.efile.core.document;

import com.efile.core.casemanagement.Case;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    Page<Document> findByType(DocumentType type, Pageable pageable);

    Page<Document> findByCaseRefId(Long caseId, Pageable pageable);

    Optional<Document> findByReceiptNumber(String receiptNumber);

    List<Document> findByCaseRef(Case caseRef);
}
