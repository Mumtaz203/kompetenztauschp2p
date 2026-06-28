package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.SkillSessionEntity;
import de.thws.kompetenz.session.domain.SessionStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    public boolean existsByMatchingRequestId(UUID matchingRequestId) {
        return count("matchingRequestId = ?1", matchingRequestId) > 0;
    }
    public Optional<SkillSessionEntity> findByMatchingRequestId(UUID matchingRequestId) {
        return find("matchingRequestId", matchingRequestId).firstResultOptional();
    }

    public List<SkillSessionEntity> findRatingOpenSessionsWithExpiredWindow(LocalDateTime now) {
        return list(
                "status = ?1 and ratingWindowEndsAt < ?2",
                SessionStatus.RATING_OPEN,
                now
        );
    }

    public List<SkillSessionEntity> findActiveSessionsAcceptedBefore(LocalDateTime cutoff) {
        return list(
                "status = ?1 and acceptedAt <= ?2",
                SessionStatus.ACTIVE,
                cutoff
        );
    }

    public List<SkillSessionEntity> findCompletionPendingSessionsWithStaleResponses(LocalDateTime cutoff) {
        return find(
                """
                select distinct s
                from SkillSessionEntity s
                where s.status = ?1
                and exists (
                    select 1
                    from SessionCompletionResponseEntity r
                    where r.sessionId = s.id
                    and r.updatedAt <= ?2
                )
                """,
                SessionStatus.COMPLETION_CONFIRMATION_PENDING,
                cutoff
        ).list();
    }
}
