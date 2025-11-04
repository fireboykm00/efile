package com.efile.core.communication.dto;

import com.efile.core.communication.CommunicationType;
import com.efile.core.user.dto.UserSummary;
import java.time.Instant;

public record CommunicationResponse(
    Long id,
    CommunicationType type,
    String content,
    boolean isRead,
    UserSummary sender,
    UserSummary recipient,
    Long caseId,
    Instant sentAt,
    Instant readAt
) {}
