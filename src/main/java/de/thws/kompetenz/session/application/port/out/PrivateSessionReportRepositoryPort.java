package de.thws.kompetenz.session.application.port.out;

import de.thws.kompetenz.session.domain.PrivateSessionReport;

import java.util.List;
import java.util.UUID;

public interface PrivateSessionReportRepositoryPort {

    PrivateSessionReport save(PrivateSessionReport report);

    List<PrivateSessionReport> findAll();

    List<PrivateSessionReport> findBySessionId(UUID sessionId);

    boolean existsBySessionIdAndReporterUserIdAndReportedUserId(
            UUID sessionId,
            UUID reporterUserId,
            UUID reportedUserId
    );

    long countByReportedUserId(UUID reportedUserId);
}
