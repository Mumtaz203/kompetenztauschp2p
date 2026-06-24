package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SessionCompletionResponseEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionCompletionJpaRepository implements PanacheRepositoryBase<SessionCompletionResponseEntity, UUID> {

    public Optional<SessionCompletionResponseEntity> findBySessionIdAndUserId(UUID sessionId, UUID userId){
        return find(
                "sessionId = ?1 and userId = ?2",
                sessionId,
                userId
        ).firstResultOptional();
    }

    public List<SessionCompletionResponseEntity> findBySessionId(UUID sessionId){
        return list(
                "sessionId = ?1",
                sessionId
        );
    }

}
