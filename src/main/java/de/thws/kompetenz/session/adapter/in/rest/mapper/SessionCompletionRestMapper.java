package de.thws.kompetenz.session.adapter.in.rest.mapper;

import de.thws.kompetenz.session.adapter.in.rest.dto.SessionCompletionResultResponse;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionCompletionRestMapper {

    public SessionCompletionResultResponse toResponse(SkillSession session){
        if( session == null ){
            return null;
        }

        return new SessionCompletionResultResponse(
                session.getId(),
                session.getMatchingRequestId(),
                session.getRequesterUserId(),
                session.getReceiverUserId(),
                session.getStatus(),
                session.getAcceptedAt(),
                session.getCompletedAt(),
                session.getRatingWindowOpenedAt(),
                session.getRatingWindowEndsAt()
        );
    }
}
