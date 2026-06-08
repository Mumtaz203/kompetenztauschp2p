package de.thws.kompetenz.rating.adapter.in.rest.mapper;

import de.thws.kompetenz.rating.adapter.in.rest.dto.CreateSessionRatingRequest;
import de.thws.kompetenz.rating.adapter.in.rest.dto.RatingSummaryResponce;
import de.thws.kompetenz.rating.adapter.in.rest.dto.SessionRatingResponse;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.rating.domain.SessionRating;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SessionRatingMapper {

    public SessionRatingResponse toResponse(SessionRating sessionRating){
        if ( sessionRating == null ){
            return null;
        }
        return new SessionRatingResponse(
                sessionRating.getId(),
                sessionRating.getSessionId(),
                sessionRating.getSenderUserId(),
                sessionRating.getReceiverUserId(),
                sessionRating.getStatus(),
                sessionRating.getPoints(),
                sessionRating.getComment(),
                sessionRating.getCreatedAt(),
                sessionRating.getPublishedAt()
        );
    }

    public SessionRating toDomain(CreateSessionRatingRequest request, UUID senderUserID){
        if( request == null ){
            return null;
        }
        return SessionRating.create(
                request.sessionId(),
                senderUserID,
                request.receiverUserId(),
                request.points(),
                request.comment()
        );
    }

    public RatingSummaryResponce toSummaryResponse(RatingSummary summary) {
        return new RatingSummaryResponce(
                summary.averagePoints(),
                summary.ratingCount()
        );
    }
}
