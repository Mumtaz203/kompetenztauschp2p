package de.thws.kompetenz.rating.application.in;

import de.thws.kompetenz.rating.domain.SessionRating;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICreateSessionRatingUseCase {

    SessionRating createRating(UUID sessionId, UUID senderUserId, UUID receiverUserId, BigDecimal points, String comment);
}
