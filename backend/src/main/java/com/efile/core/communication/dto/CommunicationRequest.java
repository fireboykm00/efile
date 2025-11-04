package com.efile.core.communication.dto;

import com.efile.core.communication.CommunicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommunicationRequest(
    @NotNull(message = "Type is required")
    CommunicationType type,

    @NotBlank(message = "Content is required")
    String content,

    @NotNull(message = "Recipient ID is required")
    Long recipientId,

    @NotNull(message = "Case ID is required")
    Long caseId
) {}
