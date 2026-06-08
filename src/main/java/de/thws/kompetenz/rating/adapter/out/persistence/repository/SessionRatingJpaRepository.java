package de.thws.kompetenz.rating.adapter.out.persistence.repository;

import de.thws.kompetenz.rating.adapter.out.persistence.entity.SessionRatingEntity;
import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.SessionRating;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionRatingJpaRepository implements PanacheRepositoryBase<SessionRatingEntity, UUID> {

    public Optional<SessionRatingEntity> findSessionRatingByID(UUID sessionRatingID){
        return find("id", sessionRatingID).firstResultOptional();
    }
    public boolean existsBySessionIdAndSenderUserId(UUID sessionId, UUID senderUserId) {
        return count(
                "sessionId = ?1 and senderUserId = ?2",
                sessionId,
                senderUserId
        ) > 0;
    }

    public List<SessionRatingEntity> findPendingRatingsBySessionId(UUID sessionId) {
        return list(
                "sessionId = ?1 and status = ?2",
                sessionId,
                RatingStatus.PENDING
        );
    }

    public BigDecimal sumPublishedPointsByReceiverUserId(UUID receiverUserId) {
        BigDecimal sum = getEntityManager()
                .createQuery(
                        """
                        select coalesce(sum(r.points), 0)
                        from SessionRatingEntity r
                        where r.receiverUserId = :receiverUserId
                        and r.status = :status
                        """,
                        BigDecimal.class
                )
                .setParameter("receiverUserId", receiverUserId)
                .setParameter("status", RatingStatus.PUBLISHED)
                .getSingleResult();

        return sum;
    }

    public long countPublishedRatingsByReceiverUserId(UUID receiverUserId) {
        return count(
                "receiverUserId = ?1 and status = ?2",
                receiverUserId,
                RatingStatus.PUBLISHED
        );
    }
}