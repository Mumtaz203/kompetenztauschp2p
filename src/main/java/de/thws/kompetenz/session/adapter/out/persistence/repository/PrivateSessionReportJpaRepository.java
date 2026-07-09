package de.thws.kompetenz.session.adapter.out.persistence.repository;

import de.thws.kompetenz.session.adapter.out.persistence.entity.PrivateSessionReportEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PrivateSessionReportJpaRepository implements PanacheRepositoryBase<PrivateSessionReportEntity, UUID> {

    public List<PrivateSessionReportEntity> findBySessionId(UUID sessionId) {
        return list("sessionId = ?1 order by createdAt desc", sessionId);
    }

    public boolean existsBySessionIdAndReporterUserIdAndReportedUserId(
            UUID sessionId,
            UUID reporterUserId,
            UUID reportedUserId
    ) {
        return count(
                "sessionId = ?1 and reporterUserId = ?2 and reportedUserId = ?3",
                sessionId,
                reporterUserId,
                reportedUserId
        ) > 0;
    }

    public long countByReportedUserId(UUID reportedUserId) {
        return count("reportedUserId = ?1", reportedUserId);
    }
}
