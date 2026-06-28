package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.exception.SessionRatingNotAuthorizedException;
import de.thws.kompetenz.rating.application.exception.SessionRatingNotFoundException;
import de.thws.kompetenz.rating.application.in.*;
import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.RatingStatus;
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
                                            , IGetRatingSummaryUseCase, IGetUserRatingUseCase, IUpdateSessionRatingStatusUseCase {

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

        SessionRating savedRating = sessionRatingRepositoryPort.save(rating);

        return publishRatingsIfBothUsersRated(sessionId, savedRating);
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

    private SessionRating publishRatingsIfBothUsersRated(UUID sessionId, SessionRating savedRating) {
        List<SessionRating> pendingRatings = sessionRatingRepositoryPort.findPendingRatingsBySessionId(sessionId);

        if (pendingRatings.size() < 2) {
            return savedRating;
        }

        LocalDateTime now = LocalDateTime.now();

        for (SessionRating rating : pendingRatings) {
            rating.publish(now);
            sessionRatingRepositoryPort.save(rating);
        }

        closeRatingWindowUseCase.closeRatingWindow(sessionId);

        if (savedRating.getStatus() == RatingStatus.PENDING) {
            savedRating.publish(now);
        }

        return savedRating;
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
    @Override
    public SessionRating getRating(UUID ratingId) {
        if (ratingId == null) {
            throw new IllegalArgumentException("Rating id must not be null");
        }

        return sessionRatingRepositoryPort.findById(ratingId)
                .orElseThrow(() -> new SessionRatingNotFoundException(ratingId));
    }

    @Override
    public SessionRating getVisibleRating(UUID ratingId, UUID currentUserId, boolean isAdmin) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user id must not be null");
        }

        SessionRating rating = getRating(ratingId);

        if (isAdmin) {
            return rating;
        }
        boolean isSender = rating.getSenderUserId().equals(currentUserId);
        boolean isPublished = rating.getStatus() == RatingStatus.PUBLISHED;

        if (isPublished || isSender) {
            return rating;
        }

        throw new SessionRatingNotAuthorizedException();
    }

    @Override
    public List<SessionRating> getVisibleRatingsForUser(UUID currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user id must not be null");
        }

        return sessionRatingRepositoryPort.findVisibleRatingsForUser(currentUserId);
    }

    @Override
    public SessionRating getPublishedRating(UUID ratingId) {
        SessionRating rating = getRating(ratingId);

        if (rating.getStatus() != RatingStatus.PUBLISHED) {
            throw new IllegalArgumentException("Rating is not published");
        }

        return rating;
    }

    @Override
    public SessionRating getNonPublishedRating(UUID ratingId) {
        SessionRating rating = getRating(ratingId);

        if (rating.getStatus() == RatingStatus.PUBLISHED) {
            throw new IllegalArgumentException("Rating is already published");
        }

        return rating;
    }

    @Override
    public List<SessionRating> getAllRatings() {
        return sessionRatingRepositoryPort.findAllRatings();
    }

    @Override
    public List<SessionRating> getAllPublishedRatings() {
        return sessionRatingRepositoryPort.findAllPublishedRatings();
    }

    @Override
    public List<SessionRating> getAllNonPublishedRatings() {
        return sessionRatingRepositoryPort.findAllNonPublishedRatings();
    }

    @Override
    public List<SessionRating> getOwnRatings(UUID currentUserId) {
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user id must not be null");
        }

        return sessionRatingRepositoryPort.findOwnRatingsByUserId(currentUserId);
    }

    @Override
    public List<SessionRating> getPublishedRatingsForUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }

        return sessionRatingRepositoryPort.findPublishedRatingsByReceiverUserId(userId);
    }

    @Override
    public List<SessionRating> getAllRatingsForUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }

        return sessionRatingRepositoryPort.findAllRatingsByReceiverUserId(userId);
    }

    @Override
    public SessionRating updateRatingStatus(UUID ratingId, RatingStatus newStatus) {
        if (ratingId == null || newStatus == null) {
            throw new IllegalArgumentException("Rating id and status must not be null");
        }

        SessionRating rating = getRating(ratingId);

        rating.changeStatus(newStatus, LocalDateTime.now());

        return sessionRatingRepositoryPort.save(rating);
    }
}
