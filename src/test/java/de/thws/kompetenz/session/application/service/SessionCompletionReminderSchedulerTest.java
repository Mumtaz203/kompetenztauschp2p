package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SessionCompletionReminderSchedulerTest {

    @Test
    void updateCompletionConfirmationStates_shouldMarkOldActiveSessionsPendingAndStalePendingSessionsDisputed() {
        SkillSession oldActiveSession = new SkillSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                SessionStatus.ACTIVE,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(4),
                null,
                null,
                null
        );
        SkillSession stalePendingSession = new SkillSession(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                SessionStatus.COMPLETION_CONFIRMATION_PENDING,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(4),
                null,
                null,
                null
        );

        SessionCompletionReminderScheduler scheduler = new SessionCompletionReminderScheduler(
                new SchedulerSessionRepository(oldActiveSession, stalePendingSession)
        );

        scheduler.updateCompletionConfirmationStates();

        assertEquals(SessionStatus.COMPLETION_CONFIRMATION_PENDING, oldActiveSession.getStatus());
        assertEquals(SessionStatus.DISPUTED, stalePendingSession.getStatus());
    }

    private static class SchedulerSessionRepository implements ISessionRepositoryPort {
        private final SkillSession oldActiveSession;
        private final SkillSession stalePendingSession;

        private SchedulerSessionRepository(SkillSession oldActiveSession, SkillSession stalePendingSession) {
            this.oldActiveSession = oldActiveSession;
            this.stalePendingSession = stalePendingSession;
        }

        @Override
        public SkillSession save(SkillSession session) {
            return session;
        }

        @Override
        public Optional<SkillSession> findById(UUID sessionId) {
            return Optional.empty();
        }

        @Override
        public List<SkillSession> getAll() {
            return List.of(oldActiveSession, stalePendingSession);
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
            return List.of(oldActiveSession);
        }

        @Override
        public List<SkillSession> findCompletionPendingSessionsWithStaleResponses(LocalDateTime cutoff) {
            return List.of(stalePendingSession);
        }
    }
}
