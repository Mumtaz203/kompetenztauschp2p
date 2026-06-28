package de.thws.kompetenz.session.adapter.in.rest.mapper;

import de.thws.kompetenz.session.adapter.in.rest.dto.PrivateSessionReportResponse;
import de.thws.kompetenz.session.domain.PrivateSessionReport;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PrivateSessionReportRestMapper {

    public PrivateSessionReportResponse toResponse(PrivateSessionReport report) {
        if (report == null) {
            return null;
        }

        return new PrivateSessionReportResponse(
                report.getId(),
                report.getSessionId(),
                report.getReporterUserId(),
                report.getReportedUserId(),
                report.getReasonCode(),
                report.getDescription(),
                report.getCreatedAt()
        );
    }
}
