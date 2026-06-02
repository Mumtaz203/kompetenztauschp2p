package de.thws.kompetenz.session.adapter.out.persistence.mapper;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SkillSessionEntity;
import de.thws.kompetenz.session.domain.SkillSession;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SkillSessionPersistenceMapper {

    public SkillSessionEntity toEntity(SkillSession session) {
        SkillSessionEntity entity = new SkillSessionEntity();

        entity.id = session.getId();
        entity.requesterUserId = session.getRequesterUserId();
        entity.receiverUserId = session.getReceiverUserId();
        entity.status = session.getStatus();
        entity.createdAt = session.getCreatedAt();
        entity.acceptedAt = session.getAcceptedAt();
        entity.completedAt = session.getCompletedAt();
        entity.ratingWindowOpenedAt = session.getRatingWindowOpenedAt();
        entity.ratingWindowEndsAt = session.getRatingWindowEndsAt();

        return entity;
    }

    public SkillSession toDomain(SkillSessionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new SkillSession(
                entity.id,
                entity.requesterUserId,
                entity.receiverUserId,
                entity.status,
                entity.createdAt,
                entity.acceptedAt,
                entity.completedAt,
                entity.ratingWindowOpenedAt,
                entity.ratingWindowEndsAt
        );
    }
}