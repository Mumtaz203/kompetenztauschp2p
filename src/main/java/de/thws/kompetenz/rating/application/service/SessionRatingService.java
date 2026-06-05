package de.thws.kompetenz.rating.application.service;

import de.thws.kompetenz.rating.application.in.ICreateSessionRatingUseCase;
import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.SessionRating;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import de.thws.kompetenz.session.domain.SessionStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Transactional
@ApplicationScoped
public class SessionRatingService implements ICreateSessionRatingUseCase{

    private final SessionRatingRepositoryPort sessionRatingRepositoryPort;
    private final IGetSessionUseCase getSessionUseCase;

    public SessionRatingService(SessionRatingRepositoryPort sessionRatingRepositoryPort,
                                IGetSessionUseCase getSessionUseCase){
        this.sessionRatingRepositoryPort = sessionRatingRepositoryPort;
        this.getSessionUseCase = getSessionUseCase;
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
}
