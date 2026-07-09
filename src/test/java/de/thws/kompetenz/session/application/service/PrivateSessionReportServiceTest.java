package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.application.port.out.PrivateSessionReportRepositoryPort;
import de.thws.kompetenz.session.application.port.out.ReportedUserFlagPort;
import de.thws.kompetenz.session.domain.PrivateSessionReport;
import de.thws.kompetenz.session.domain.PrivateSessionReportReason;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class PrivateSessionReportServiceTest {

    private final UUID sessionId = UUID.randomUUID();
    private final UUID reporterId = UUID.randomUUID();
    private final UUID reportedId = UUID.randomUUID();

    @Test
    void createReport_shouldSavePrivateReport_whenUsersBelongToSession() {
        InMemoryReportRepository reportRepository = new InMemoryReportRepository(1);
        AtomicLong flagCount = new AtomicLong();
        AtomicBoolean flagged = new AtomicBoolean();

        PrivateSessionReportService service = new PrivateSessionReportService(
                reportRepository,
                new SingleSessionRepository(),
                new CapturingFlagPort(flagCount, flagged)
        );

        PrivateSessionReport result = service.createReport(
                sessionId,
                reporterId,
                reportedId,
                PrivateSessionReportReason.NO_SHOW,
                "Did not appear"
        );

        assertEquals(sessionId, result.getSessionId());
        assertEquals(reporterId, result.getReporterUserId());
        assertEquals(reportedId, result.getReportedUserId());
        assertEquals(1, flagCount.get());
        assertFalse(flagged.get());
    }

    @Test
    void createReport_shouldFlagReportedUser_whenThresholdIsReached() {
        InMemoryReportRepository reportRepository = new InMemoryReportRepository(3);
        AtomicBoolean flagged = new AtomicBoolean();

        PrivateSessionReportService service = new PrivateSessionReportService(
                reportRepository,
                new SingleSessionRepository(),
                new CapturingFlagPort(new AtomicLong(), flagged)
        );

        service.createReport(sessionId, reporterId, reportedId, PrivateSessionReportReason.NO_RESPONSE, null);

        assertTrue(flagged.get());
    }

    @Test
    void createReport_shouldRejectNonParticipant() {
        PrivateSessionReportService service = new PrivateSessionReportService(
                new InMemoryReportRepository(0),
                new SingleSessionRepository(),
                new CapturingFlagPort(new AtomicLong(), new AtomicBoolean())
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.createReport(
                        sessionId,
                        reporterId,
                        UUID.randomUUID(),
                        PrivateSessionReportReason.OTHER,
                        "Unknown user"
                )
        );
    }

    @Test
    void createReport_shouldRejectDuplicateReportForSameSessionAndUsers() {
        InMemoryReportRepository reportRepository = new InMemoryReportRepository(1);
        PrivateSessionReportService service = new PrivateSessionReportService(
                reportRepository,
                new SingleSessionRepository(),
                new CapturingFlagPort(new AtomicLong(), new AtomicBoolean())
        );

        service.createReport(
                sessionId,
                reporterId,
                reportedId,
                PrivateSessionReportReason.NO_SHOW,
                "First report"
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                service.createReport(
                        sessionId,
                        reporterId,
                        reportedId,
                        PrivateSessionReportReason.NO_RESPONSE,
                        "Second report"
                )
        );

        assertEquals("You have already reported this user for this session", exception.getMessage());
        assertEquals(1, reportRepository.reports.size());
    }

    @Test
    void hasReportFromUser_shouldReturnTrue_whenReportExistsForSameSessionAndUsers() {
        InMemoryReportRepository reportRepository = new InMemoryReportRepository(1);
        PrivateSessionReportService service = new PrivateSessionReportService(
                reportRepository,
                new SingleSessionRepository(),
                new CapturingFlagPort(new AtomicLong(), new AtomicBoolean())
        );

        service.createReport(sessionId, reporterId, reportedId, PrivateSessionReportReason.NO_SHOW, null);

        assertTrue(service.hasReportFromUser(sessionId, reporterId, reportedId));
        assertFalse(service.hasReportFromUser(sessionId, reportedId, reporterId));
    }

    private static class CapturingFlagPort implements ReportedUserFlagPort {
        private final AtomicLong reportCount;
        private final AtomicBoolean flagged;

        private CapturingFlagPort(AtomicLong reportCount, AtomicBoolean flagged) {
            this.reportCount = reportCount;
            this.flagged = flagged;
        }

        @Override
        public void updateReportCount(UUID userId, long reportCount) {
            this.reportCount.set(reportCount);
        }

        @Override
        public void flagUser(UUID userId) {
            flagged.set(true);
        }
    }

    private class SingleSessionRepository implements ISessionRepositoryPort {
        private final SkillSession session = new SkillSession(
                sessionId,
                UUID.randomUUID(),
                reporterId,
                reportedId,
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null
        );

        @Override
        public SkillSession save(SkillSession session) {
            return session;
        }

        @Override
        public Optional<SkillSession> findById(UUID sessionId) {
            return this.session.getId().equals(sessionId) ? Optional.of(session) : Optional.empty();
        }

        @Override
        public List<SkillSession> getAll() {
            return List.of(session);
        }

        @Override
        public boolean existsActiveSessionBetween(UUID requesterUserId, UUID receiverUserId) {
            return false;
        }

        @Override
        public boolean existsByMatchingRequestId(UUID matchingRequestId) {
            return false;
        }

        @Override
        public Optional<SkillSession> findByMatchingRequestId(UUID matchingRequestId) {
            return Optional.empty();
        }

        @Override
        public List<SkillSession> findRatingOpenSessionsWithExpiredWindow(LocalDateTime now) {
            return List.of();
        }

        @Override
        public List<SkillSession> findActiveSessionsAcceptedBefore(LocalDateTime cutoff) {
            return List.of();
        }

        @Override
        public List<SkillSession> findCompletionPendingSessionsWithStaleResponses(LocalDateTime cutoff) {
            return List.of();
        }
    }

    private static class InMemoryReportRepository implements PrivateSessionReportRepositoryPort {
        private final List<PrivateSessionReport> reports = new ArrayList<>();
        private final long countAfterSave;

        private InMemoryReportRepository(long countAfterSave) {
            this.countAfterSave = countAfterSave;
        }

        @Override
        public PrivateSessionReport save(PrivateSessionReport report) {
            reports.add(report);
            return report;
        }

        @Override
        public List<PrivateSessionReport> findAll() {
            return reports;
        }

        @Override
        public List<PrivateSessionReport> findBySessionId(UUID sessionId) {
            return reports.stream()
                    .filter(report -> report.getSessionId().equals(sessionId))
                    .toList();
        }

        @Override
        public boolean existsBySessionIdAndReporterUserIdAndReportedUserId(
                UUID sessionId,
                UUID reporterUserId,
                UUID reportedUserId
        ) {
            return reports.stream()
                    .anyMatch(report ->
                            report.getSessionId().equals(sessionId)
                                    && report.getReporterUserId().equals(reporterUserId)
                                    && report.getReportedUserId().equals(reportedUserId)
                    );
        }

        @Override
        public long countByReportedUserId(UUID reportedUserId) {
            return countAfterSave;
        }
    }
}
