package de.thws.kompetenz.session.adapter.in.rest.dto;

import de.thws.kompetenz.session.domain.PrivateSessionReportReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record PrivateSessionReportResponse(
        UUID id,
        UUID sessionId,
        UUID reporterUserId,
        UUID reportedUserId,
        PrivateSessionReportReason reasonCode,
        String description,
        LocalDateTime createdAt
) {
}
