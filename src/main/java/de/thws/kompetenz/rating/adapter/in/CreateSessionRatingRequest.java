package de.thws.kompetenz.rating.adapter.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSessionRatingRequest(
        UUID sessionId,
        UUID senderUserId,
        UUID receiverUserId,
        BigDecimal points,
        String comment
) {
}
