package de.thws.kompetenz.session.adapter.out.persistence.mapper;

import de.thws.kompetenz.session.adapter.out.persistence.entity.PrivateSessionReportEntity;
import de.thws.kompetenz.session.domain.PrivateSessionReport;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrivateSessionReportPersistenceMapper {

    public PrivateSessionReportEntity toEntity(PrivateSessionReport report) {
        if (report == null) {
            return null;
        }

        PrivateSessionReportEntity entity = new PrivateSessionReportEntity();
        entity.id = report.getId();
        entity.sessionId = report.getSessionId();
        entity.reporterUserId = report.getReporterUserId();
        entity.reportedUserId = report.getReportedUserId();
        entity.reasonCode = report.getReasonCode();
        entity.description = report.getDescription();
        entity.createdAt = report.getCreatedAt();

        return entity;
    }

    public PrivateSessionReport toDomain(PrivateSessionReportEntity entity) {
        if (entity == null) {
            return null;
        }

        return new PrivateSessionReport(
                entity.id,
                entity.sessionId,
                entity.reporterUserId,
                entity.reportedUserId,
                entity.reasonCode,
                entity.description,
                entity.createdAt
        );
    }
}
