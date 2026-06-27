package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SkillSessionEntity;
import de.thws.kompetenz.session.adapter.out.persistence.mapper.SkillSessionPersistenceMapper;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SkillSessionRepositoryAdapter implements ISessionRepositoryPort {

    private final SkillSessionJpaRepository repository;
    private final SkillSessionPersistenceMapper mapper;

    public SkillSessionRepositoryAdapter(
            SkillSessionJpaRepository repository,
            SkillSessionPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SkillSession save(SkillSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session must not be null");
        }

        if (session.getId() == null) {
            SkillSessionEntity entity = mapper.toEntity(session);
            repository.persist(entity);
            return mapper.toDomain(entity);
        }

        Optional<SkillSessionEntity> existingEntityOptional = repository.findByIdOptional(session.getId());

        if (existingEntityOptional.isEmpty()) {
            SkillSessionEntity entity = mapper.toEntity(session);
            repository.persist(entity);
            return mapper.toDomain(entity);
        }

        SkillSessionEntity existingEntity = existingEntityOptional.get();

        existingEntity.status = session.getStatus();
        existingEntity.completedAt = session.getCompletedAt();
        existingEntity.ratingWindowOpenedAt = session.getRatingWindowOpenedAt();
        existingEntity.ratingWindowEndsAt = session.getRatingWindowEndsAt();

        return mapper.toDomain(existingEntity);
    }

    @Override
    public List<SkillSession> getAll(){
        return repository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<SkillSession> findById(UUID sessionId) {
        return repository.findByIdOptional(sessionId)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsActiveSessionBetween(UUID requesterUserId, UUID receiverUserId) {
        return repository.existsActiveSessionBetween(requesterUserId, receiverUserId);
    }

    @Override
    public boolean existsByMatchingRequestId(UUID matchingRequestId) {
        return repository.existsByMatchingRequestId(matchingRequestId);
    }

    @Override
    public Optional<SkillSession> findByMatchingRequestId(UUID matchingRequestId) {
        return repository.findByMatchingRequestId(matchingRequestId)
                .map(mapper::toDomain);
    }

    @Override
    public List<SkillSession> findRatingOpenSessionsWithExpiredWindow(LocalDateTime now) {
        return repository.findRatingOpenSessionsWithExpiredWindow(now)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SkillSession> findActiveSessionsAcceptedBefore(LocalDateTime cutoff) {
        return repository.findActiveSessionsAcceptedBefore(cutoff)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<SkillSession> findCompletionPendingSessionsWithStaleResponses(LocalDateTime cutoff) {
        return repository.findCompletionPendingSessionsWithStaleResponses(cutoff)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
