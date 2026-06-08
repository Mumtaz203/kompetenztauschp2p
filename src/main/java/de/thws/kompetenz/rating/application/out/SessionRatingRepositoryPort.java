package de.thws.kompetenz.rating.application.out;

import de.thws.kompetenz.rating.domain.SessionRating;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRatingRepositoryPort {

    SessionRating save(SessionRating sessionRating);
    Optional<SessionRating> findById(UUID sessionRatingId);

    boolean existsBySessionIdAndSenderUserId(UUID sessionId, UUID senderUserId);

    List<SessionRating> findPendingRatingsBySessionId(UUID sessionId);


    BigDecimal sumPublishedPointsByReceiverUserId(UUID receiverUserId);

    long countPublishedRatingsByReceiverUserId(UUID receiverUserId);

}
