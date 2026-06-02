package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.mapper.SkillSessionPersistenceMapper;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;

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
        var entity = mapper.toEntity(session);
        repository.persist(entity);
        return mapper.toDomain(entity);
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
}
