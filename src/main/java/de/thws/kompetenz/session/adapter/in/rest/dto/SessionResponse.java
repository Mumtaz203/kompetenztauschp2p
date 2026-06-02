package de.thws.kompetenz.session.adapter.in.rest.dto;

import de.thws.kompetenz.session.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID id,
        UUID requesterUserId,
        UUID receiverUserId,
        SessionStatus status,
        LocalDateTime createdAt,
        LocalDateTime acceptedAt,
        LocalDateTime completedAt,
        LocalDateTime ratingWindowOpenedAt,
        LocalDateTime ratingWindowEndsAt
) {
}
