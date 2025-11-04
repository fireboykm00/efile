package com.efile.core.communication;

import com.efile.core.casemanagement.Case;
import com.efile.core.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunicationRepository extends JpaRepository<Communication, Long> {

    List<Communication> findByRecipientIdOrderBySentAtDesc(Long recipientId);

    List<Communication> findBySenderIdOrderBySentAtDesc(Long senderId);

    List<Communication> findByCaseRefIdOrderBySentAtAsc(Long caseId);

    long countByRecipientIdAndIsReadFalse(Long recipientId);

    List<Communication> findBySenderOrRecipient(User sender, User recipient);

    List<Communication> findByCaseRef(Case caseRef);

    long countByRecipientAndIsRead(User recipient, boolean isRead);
}
