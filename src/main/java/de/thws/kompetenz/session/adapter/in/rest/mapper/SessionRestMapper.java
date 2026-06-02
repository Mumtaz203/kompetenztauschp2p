package de.thws.kompetenz.session.adapter.in.rest.mapper;

import de.thws.kompetenz.session.adapter.in.rest.dto.SessionResponse;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionRestMapper {

    public SessionResponse toResponse(SkillSession session) {
        return new SessionResponse(
                session.getId(),
                session.getRequesterUserId(),
                session.getReceiverUserId(),
                session.getStatus(),
                session.getCreatedAt(),
                session.getAcceptedAt(),
                session.getCompletedAt(),
                session.getRatingWindowOpenedAt(),
                session.getRatingWindowEndsAt()
        );
    }
}
