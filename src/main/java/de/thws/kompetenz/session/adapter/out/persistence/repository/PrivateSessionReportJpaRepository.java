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

    public long countByReportedUserId(UUID reportedUserId) {
        return count("reportedUserId = ?1", reportedUserId);
    }
}
