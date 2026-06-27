package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.PrivateSessionReport;
import de.thws.kompetenz.session.domain.PrivateSessionReportReason;

import java.util.UUID;

public interface ICreatePrivateSessionReportUseCase {

    PrivateSessionReport createReport(
            UUID sessionId,
            UUID reporterUserId,
            UUID reportedUserId,
            PrivateSessionReportReason reasonCode,
            String description
    );
}
