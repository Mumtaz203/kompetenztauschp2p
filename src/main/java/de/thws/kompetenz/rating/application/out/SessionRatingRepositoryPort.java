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

    List<SessionRating> findAllRatings();

    List<SessionRating> findAllPublishedRatings();

    List<SessionRating> findAllNonPublishedRatings();

    List<SessionRating> findOwnRatingsByUserId(UUID userId);

    List<SessionRating> findPublishedRatingsByReceiverUserId(UUID receiverUserId);

    List<SessionRating> findVisibleRatingsForUser(UUID userId);

    List<SessionRating> findAllRatingsByReceiverUserId(UUID receiverUserId);


    BigDecimal sumPublishedPointsByReceiverUserId(UUID receiverUserId);

    long countPublishedRatingsByReceiverUserId(UUID receiverUserId);

}
