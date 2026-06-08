package de.thws.kompetenz.rating.adapter.in.rest.dto;

import de.thws.kompetenz.rating.domain.RatingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SessionRatingResponse(
        UUID id,
        UUID sessionId,
        UUID senderUserId,
        UUID receiverUserId,
        RatingStatus status,
        BigDecimal points,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime publishedAt
) {
}
