package de.thws.kompetenz.session.adapter.out.persistence.mapper;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SessionCompletionResponseEntity;
import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import de.thws.kompetenz.session.domain.SessionCompletionResponse;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionCompletionPersistenceMapper {

    public SessionCompletionResponseEntity toEntity(SessionCompletionResponse sessionCompletionResponse){

        if(sessionCompletionResponse == null){
            return null;
        }

        SessionCompletionResponseEntity entity = new SessionCompletionResponseEntity();

        entity.id = sessionCompletionResponse.getId();
        entity.sessionId = sessionCompletionResponse.getSessionId();
        entity.userId = sessionCompletionResponse.getUserId();
        entity.answer = sessionCompletionResponse.getAnswer();
        entity.reason = sessionCompletionResponse.getReason();
        entity.createdAt = sessionCompletionResponse.getCreatedAt();
        entity.updatedAt = sessionCompletionResponse.getUpdatedAt();

        return entity;
    }

    public SessionCompletionResponse toDomain(SessionCompletionResponseEntity entity){
        if(entity == null) return null;

        return new SessionCompletionResponse(
                entity.id,
                entity.sessionId,
                entity.userId,
                entity.answer,
                entity.reason,
                entity.createdAt,
                entity.updatedAt
        );
    }
}
