package com.efile.core.document;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentStatusHistoryRepository extends JpaRepository<DocumentStatusHistory, Long> {

    List<DocumentStatusHistory> findByDocumentIdOrderByChangedAtAsc(Long documentId);

    void deleteByDocumentId(Long documentId);
}
