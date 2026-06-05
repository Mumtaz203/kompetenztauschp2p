package de.thws.kompetenz.rating.adapter.out.persistence.mapper;

import de.thws.kompetenz.rating.adapter.out.persistence.entity.SessionRatingEntity;
import de.thws.kompetenz.rating.domain.SessionRating;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionRatingPersistenceMapper {

    public SessionRating toDomain(SessionRatingEntity entity){
        if (entity == null) {
            return null;
        }

        return new SessionRating(
                entity.getId(),
                entity.getSessionId(),
                entity.getSenderUserId(),
                entity.getReceiverUserId(),
                entity.getStatus(),
                entity.getPoints(),
                entity.getComment(),
                entity.getCreatedAt(),
                entity.getPublishedAt());
    }

    public SessionRatingEntity toEntity(SessionRating rating)
    {
        if (rating == null) {
            return null;
        }

        return new SessionRatingEntity(
                rating.getId(),
                rating.getSessionId(),
                rating.getSenderUserId(),
                rating.getReceiverUserId(),
                rating.getStatus(),
                rating.getPoints(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getPublishedAt()
                );
    }
}
