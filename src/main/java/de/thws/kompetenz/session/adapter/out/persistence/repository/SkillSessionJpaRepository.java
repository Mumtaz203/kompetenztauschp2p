package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SkillSessionEntity;
import de.thws.kompetenz.session.domain.SessionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SkillSessionJpaRepository implements PanacheRepositoryBase<SkillSessionEntity, UUID> {

    public boolean existsActiveSessionBetween(UUID firstUserId, UUID secondUserId) {
        return count(
                """
                status = ?1 and (
                    (requesterUserId = ?2 and receiverUserId = ?3)
                    or
                    (requesterUserId = ?3 and receiverUserId = ?2)
                )
                """,
                SessionStatus.ACTIVE,
                firstUserId,
                secondUserId
        ) > 0;
    }
}