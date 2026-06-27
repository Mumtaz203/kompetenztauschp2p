package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.out.ISessionCompletionResponseRepositoryPort;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import de.thws.kompetenz.session.domain.SessionCompletionResponse;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionCompletionServiceTest {

    private final UUID sessionId = UUID.randomUUID();
    private final UUID requesterId = UUID.randomUUID();
    private final UUID receiverId = UUID.randomUUID();
    private InMemorySessionRepository sessionRepository;
    private InMemoryCompletionRepository completionRepository;
    private SessionCompletionService service;

    @BeforeEach
    void setUp() {
        sessionRepository = new InMemorySessionRepository(
                SkillSession.create(UUID.randomUUID(), requesterId, receiverId)
        );
        completionRepository = new InMemoryCompletionRepository();
        service = new SessionCompletionService(sessionRepository, completionRepository);
    }

    @Test
    void submitCompletionResponse_shouldOpenRatingWindow_whenBothUsersCompleted() {
        service.submitCompletionResponse(sessionId, requesterId, SessionCompletionAnswer.COMPLETED, null);

        SkillSession result = service.submitCompletionResponse(sessionId, receiverId, SessionCompletionAnswer.COMPLETED, null);

        assertEquals(SessionStatus.RATING_OPEN, result.getStatus());
        assertNotNull(result.getCompletedAt());
        assertNotNull(result.getRatingWindowOpenedAt());
        assertNotNull(result.getRatingWindowEndsAt());
    }

    @Test
    void submitCompletionResponse_shouldKeepSessionActive_whenBothUsersSayNotYet() {
        service.submitCompletionResponse(sessionId, requesterId, SessionCompletionAnswer.NOT_YET, null);

        SkillSession result = service.submitCompletionResponse(sessionId, receiverId, SessionCompletionAnswer.NOT_YET, null);

        assertEquals(SessionStatus.ACTIVE, result.getStatus());
        assertNull(result.getRatingWindowOpenedAt());
    }

    @Test
    void submitCompletionResponse_shouldCancelSession_whenBothUsersCancel() {
        service.submitCompletionResponse(sessionId, requesterId, SessionCompletionAnswer.CANCELLED, "No time");

        SkillSession result = service.submitCompletionResponse(sessionId, receiverId, SessionCompletionAnswer.CANCELLED, "No time");

        assertEquals(SessionStatus.CANCELLED, result.getStatus());
        assertNull(result.getRatingWindowOpenedAt());
    }

    @Test
    void submitCompletionResponse_shouldMarkPending_whenCompletedAndNotYet() {
        service.submitCompletionResponse(sessionId, requesterId, SessionCompletionAnswer.COMPLETED, null);

        SkillSession result = service.submitCompletionResponse(sessionId, receiverId, SessionCompletionAnswer.NOT_YET, null);

        assertEquals(SessionStatus.COMPLETION_CONFIRMATION_PENDING, result.getStatus());
        assertNull(result.getRatingWindowOpenedAt());
    }

    @Test
    void submitCompletionResponse_shouldMarkDisputed_whenProblemIsReported() {
        SkillSession result = service.submitCompletionResponse(sessionId, requesterId, SessionCompletionAnswer.PROBLEM, "No show");

        assertEquals(SessionStatus.DISPUTED, result.getStatus());
        assertNull(result.getRatingWindowOpenedAt());
    }

    @Test
    void submitCompletionResponse_shouldRejectNonParticipant() {
        assertThrows(IllegalArgumentException.class, () ->
                service.submitCompletionResponse(sessionId, UUID.randomUUID(), SessionCompletionAnswer.COMPLETED, null)
        );
    }

    private class InMemorySessionRepository implements ISessionRepositoryPort {
        private SkillSession session;

        private InMemorySessionRepository(SkillSession session) {
            this.session = new SkillSession(
                    sessionId,
                    session.getMatchingRequestId(),
                    session.getRequesterUserId(),
                    session.getReceiverUserId(),
                    session.getStatus(),
                    session.getCreatedAt(),
                    session.getAcceptedAt(),
                    session.getCompletedAt(),
                    session.getRatingWindowOpenedAt(),
                    session.getRatingWindowEndsAt()
            );
        }

        @Override
        public SkillSession save(SkillSession session) {
            this.session = session;
            return session;
        }

        @Override
        public Optional<SkillSession> findById(UUID sessionId) {
            return this.session.getId().equals(sessionId) ? Optional.of(this.session) : Optional.empty();
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

    private static class InMemoryCompletionRepository implements ISessionCompletionResponseRepositoryPort {
        private final List<SessionCompletionResponse> responses = new ArrayList<>();

        @Override
        public SessionCompletionResponse save(SessionCompletionResponse response) {
            responses.removeIf(existing -> existing.getSessionId().equals(response.getSessionId())
                    && existing.getUserId().equals(response.getUserId()));
            responses.add(response);
            return response;
        }

        @Override
        public Optional<SessionCompletionResponse> findBySessionIdAndUserId(UUID sessionId, UUID userId) {
            return responses.stream()
                    .filter(response -> response.getSessionId().equals(sessionId)
                            && response.getUserId().equals(userId))
                    .findFirst();
        }

        @Override
        public List<SessionCompletionResponse> findBySessionId(UUID sessionId) {
            return responses.stream()
                    .filter(response -> response.getSessionId().equals(sessionId))
                    .toList();
        }
    }
}
