package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.in.ICreateSessionRatingUseCase;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.rating.domain.SessionRating;
import de.thws.kompetenz.session.application.port.in.ICloseRatingWindowUseCase;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import de.thws.kompetenz.session.domain.SessionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class SessionRatingService implements ICreateSessionRatingUseCase, IPublishSessionRatingsUseCase
                                            , IGetRatingSummaryUseCase {

    private final SessionRatingRepositoryPort sessionRatingRepositoryPort;
    private final IGetSessionUseCase getSessionUseCase;
    private final ICloseRatingWindowUseCase closeRatingWindowUseCase;

    public SessionRatingService(SessionRatingRepositoryPort sessionRatingRepositoryPort,
                                IGetSessionUseCase getSessionUseCase,
                                ICloseRatingWindowUseCase closeRatingWindowUseCase){
        this.sessionRatingRepositoryPort = sessionRatingRepositoryPort;
        this.getSessionUseCase = getSessionUseCase;
        this.closeRatingWindowUseCase = closeRatingWindowUseCase;
    }


    @Override
    public SessionRating createRating(
            UUID sessionId,
            UUID senderUserId,
            UUID receiverUserId,
            BigDecimal points,
            String comment
    ) {
        if (sessionId == null || senderUserId == null || receiverUserId == null) {
            throw new IllegalArgumentException("Session id, sender id and receiver id must not be null");
        }

        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("A user cannot rate himself");
        }

        var session = getSessionUseCase.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.hasParticipant(senderUserId)) {
            throw new IllegalArgumentException("Sender is not a participant of this session");
        }

        if (!session.hasParticipant(receiverUserId)) {
            throw new IllegalArgumentException("Receiver is not a participant of this session");
        }

        if (!session.isBetween(senderUserId, receiverUserId)) {
            throw new IllegalArgumentException("Users do not belong to this session");
        }

        if (session.getStatus() != SessionStatus.RATING_OPEN) {
            throw new IllegalStateException("Rating is only allowed when the session is rating open");
        }

        if (LocalDateTime.now().isAfter(session.getRatingWindowEndsAt())) {
            throw new IllegalStateException("Rating window is closed");
        }

        if (sessionRatingRepositoryPort.existsBySessionIdAndSenderUserId(sessionId, senderUserId)) {
            throw new IllegalStateException("User has already rated this session");
        }

        SessionRating rating = SessionRating.create(
                sessionId,
                senderUserId,
                receiverUserId,
                points,
                comment
        );

        return sessionRatingRepositoryPort.save(rating);
    }
    @Override
    @Transactional
    public List<SessionRating> publishRatingsForSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        var session = getSessionUseCase.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (session.getStatus() != SessionStatus.RATING_OPEN) {
            throw new IllegalStateException("Ratings can only be published for sessions with an open rating window");
        }

        if (session.getRatingWindowEndsAt() == null) {
            throw new IllegalStateException("Rating window end is not set");
        }

        if (LocalDateTime.now().isBefore(session.getRatingWindowEndsAt())) {
            throw new IllegalStateException("Rating window is still open");
        }

        List<SessionRating> pendingRatings = sessionRatingRepositoryPort.findPendingRatingsBySessionId(sessionId);

        LocalDateTime now = LocalDateTime.now();

        List<SessionRating> publishedRatings = pendingRatings.stream()
                .map(rating -> {
                    rating.publish(now);
                    return sessionRatingRepositoryPort.save(rating);
                })
                .toList();

        closeRatingWindowUseCase.closeRatingWindow(sessionId);

        return publishedRatings;
    }

    @Override
    public RatingSummary getRatingSummaryForUser(UUID userId){

        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }

        BigDecimal sum = sessionRatingRepositoryPort.sumPublishedPointsByReceiverUserId(userId);
        long count = sessionRatingRepositoryPort.countPublishedRatingsByReceiverUserId(userId);

        if (count == 0) {
            return new RatingSummary(BigDecimal.ZERO, 0);
        }

        BigDecimal average = sum.divide(
                BigDecimal.valueOf(count),
                1,
                java.math.RoundingMode.HALF_UP
        );

        return new RatingSummary(average, count);
    }
}
