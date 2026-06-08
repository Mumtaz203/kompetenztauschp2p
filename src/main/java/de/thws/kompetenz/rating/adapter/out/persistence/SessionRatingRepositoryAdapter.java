package de.thws.kompetenz.rating.adapter.out.persistence;

import de.thws.kompetenz.rating.adapter.out.persistence.entity.SessionRatingEntity;
import de.thws.kompetenz.rating.adapter.out.persistence.mapper.SessionRatingPersistenceMapper;
import de.thws.kompetenz.rating.adapter.out.persistence.repository.SessionRatingJpaRepository;
import de.thws.kompetenz.rating.application.out.SessionRatingRepositoryPort;
import de.thws.kompetenz.rating.domain.SessionRating;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionRatingRepositoryAdapter implements SessionRatingRepositoryPort {

    private final SessionRatingJpaRepository repository;
    private final SessionRatingPersistenceMapper mapper;

    // @Inject
    public SessionRatingRepositoryAdapter(SessionRatingJpaRepository repository,
                                          SessionRatingPersistenceMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SessionRating save(SessionRating sessionRating) {
        if (sessionRating == null) {
            throw new IllegalArgumentException("No session rating to be saved, session rating is null");
        }

        if (sessionRating.getId() == null) {
            SessionRatingEntity entity = mapper.toEntity(sessionRating);
            repository.persist(entity);
            return mapper.toDomain(entity);
        }

        SessionRatingEntity existingEntity = repository.findSessionRatingByID(sessionRating.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existing SessionRating found for this id"));

        existingEntity.setStatus(sessionRating.getStatus());
        existingEntity.setPublishedAt(sessionRating.getPublishedAt());

        return mapper.toDomain(existingEntity);
    }


    @Override
    public Optional<SessionRating> findById(UUID sessionRatingId) {
        return repository.findSessionRatingByID(sessionRatingId)
                .map(mapper::toDomain);
    }

    @Override
    public List<SessionRating> findPendingRatingsBySessionId(UUID sessionId){
        return repository.findPendingRatingsBySessionId(sessionId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySessionIdAndSenderUserId(UUID sessionId,UUID senderUserId){
        return repository.existsBySessionIdAndSenderUserId(sessionId, senderUserId);
    }

    @Override
    public BigDecimal sumPublishedPointsByReceiverUserId(UUID receiverUserId){
        return repository.sumPublishedPointsByReceiverUserId(receiverUserId);
    }

    @Override
    public long countPublishedRatingsByReceiverUserId(UUID receiverUserId) {
        return repository.countPublishedRatingsByReceiverUserId(receiverUserId);
    }

}
