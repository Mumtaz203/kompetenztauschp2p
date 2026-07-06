package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.in.ICreatePrivateSessionReportUseCase;
import de.thws.kompetenz.session.application.port.in.IGetPrivateSessionReportsUseCase;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.application.port.out.PrivateSessionReportRepositoryPort;
import de.thws.kompetenz.session.application.port.out.ReportedUserFlagPort;
import de.thws.kompetenz.session.domain.PrivateSessionReport;
import de.thws.kompetenz.session.domain.PrivateSessionReportReason;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PrivateSessionReportService implements ICreatePrivateSessionReportUseCase, IGetPrivateSessionReportsUseCase {

    private static final int REPORT_FLAG_THRESHOLD = 3;

    private final PrivateSessionReportRepositoryPort reportRepositoryPort;
    private final ISessionRepositoryPort sessionRepositoryPort;
    private final ReportedUserFlagPort reportedUserFlagPort;

    public PrivateSessionReportService(
            PrivateSessionReportRepositoryPort reportRepositoryPort,
            ISessionRepositoryPort sessionRepositoryPort,
            ReportedUserFlagPort reportedUserFlagPort
    ) {
        this.reportRepositoryPort = reportRepositoryPort;
        this.sessionRepositoryPort = sessionRepositoryPort;
        this.reportedUserFlagPort = reportedUserFlagPort;
    }

    @Override
    @Transactional
    public PrivateSessionReport createReport(
            UUID sessionId,
            UUID reporterUserId,
            UUID reportedUserId,
            PrivateSessionReportReason reasonCode,
            String description
    ) {
        if (sessionId == null || reporterUserId == null || reportedUserId == null || reasonCode == null) {
            throw new IllegalArgumentException("Session id, reporter id, reported id and reason must not be null");
        }

        if (reporterUserId.equals(reportedUserId)) {
            throw new IllegalArgumentException("A user cannot report himself");
        }

        SkillSession session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.isBetween(reporterUserId, reportedUserId)) {
            throw new IllegalArgumentException("Both users must be participants of this session");
        }

        if (reportRepositoryPort.existsBySessionIdAndReporterUserIdAndReportedUserId(
                sessionId,
                reporterUserId,
                reportedUserId
        )) {
            throw new IllegalArgumentException("You have already reported this user for this session");
        }

        PrivateSessionReport savedReport = reportRepositoryPort.save(
                PrivateSessionReport.create(sessionId, reporterUserId, reportedUserId, reasonCode, description)
        );

        long reportCount = reportRepositoryPort.countByReportedUserId(reportedUserId);
        reportedUserFlagPort.updateReportCount(reportedUserId, reportCount);

        if (reportCount >= REPORT_FLAG_THRESHOLD) {
            reportedUserFlagPort.flagUser(reportedUserId);
        }

        return savedReport;
    }

    @Override
    public List<PrivateSessionReport> getAllReports() {
        return reportRepositoryPort.findAll();
    }

    @Override
    public List<PrivateSessionReport> getReportsForSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        return reportRepositoryPort.findBySessionId(sessionId);
    }

    @Override
    public boolean hasReportFromUser(UUID sessionId, UUID reporterUserId, UUID reportedUserId) {
        if (sessionId == null || reporterUserId == null || reportedUserId == null) {
            throw new IllegalArgumentException("Session id, reporter id and reported id must not be null");
        }

        return reportRepositoryPort.existsBySessionIdAndReporterUserIdAndReportedUserId(
                sessionId,
                reporterUserId,
                reportedUserId
        );
    }
}
