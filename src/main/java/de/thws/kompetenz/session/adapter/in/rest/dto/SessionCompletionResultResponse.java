package de.thws.kompetenz.session.adapter.in.rest.dto;

import de.thws.kompetenz.session.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionCompletionResultResponse(
        UUID sessionId,
        UUID matchingRequestId,
        UUID requestUserId,
        UUID receiverUserId,
        SessionStatus status,
        LocalDateTime acceptedAt,
        LocalDateTime updatedAt,
        LocalDateTime ratingWindowOpenedAt,
        LocalDateTime ratingWindowEndsAt
) {
}
