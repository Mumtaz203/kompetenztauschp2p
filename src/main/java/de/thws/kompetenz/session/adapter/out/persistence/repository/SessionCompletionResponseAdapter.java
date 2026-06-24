package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SessionCompletionResponseEntity;
import de.thws.kompetenz.session.adapter.out.persistence.mapper.SessionCompletionPersistenceMapper;
import de.thws.kompetenz.session.application.port.out.ISessionCompletionResponseRepositoryPort;
import de.thws.kompetenz.session.domain.SessionCompletionResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionCompletionResponseAdapter implements ISessionCompletionResponseRepositoryPort {

    @Inject
    SessionCompletionJpaRepository sessionCompletionJpaRepository;
    @Inject
    SessionCompletionPersistenceMapper mapper;

    @Override
    public SessionCompletionResponse save(SessionCompletionResponse response){

        if( response == null ){
            throw new IllegalArgumentException("Session completion response cannot be null!");
        }

        SessionCompletionResponseEntity entity = mapper.toEntity(response);

        Optional<SessionCompletionResponseEntity> existingEntity = sessionCompletionJpaRepository.findByIdOptional(
                entity.sessionId
        );

        if(existingEntity.isPresent()){
            SessionCompletionResponseEntity existing = existingEntity.get();

            existing.sessionId = entity.sessionId;
            existing.userId = entity.userId;
            existing.answer = entity.answer;
            existing.reason = entity.reason;
            existing.createdAt = entity.createdAt;
            existing.updatedAt = entity.updatedAt;

            return mapper.toDomain(existing);
        }
        sessionCompletionJpaRepository.persist(entity);
        return mapper.toDomain(entity);
    }

    @Override
    public Optional<SessionCompletionResponse> findBySessionIdAndUserId(UUID sessionId, UUID userId){
        if(sessionId == null ||userId == null){
            throw new IllegalArgumentException("Session id and user id must not be null!");
        }
        return sessionCompletionJpaRepository.findBySessionIdAndUserId(sessionId,userId).map(mapper::toDomain);
    }

    @Override
    public List<SessionCompletionResponse> findBySessionId(UUID sessionId){
        if( sessionId == null ){
            throw new IllegalArgumentException("Session id must not be null!");
        }

        return sessionCompletionJpaRepository.findBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
