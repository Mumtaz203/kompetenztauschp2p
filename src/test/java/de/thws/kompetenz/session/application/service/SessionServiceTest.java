package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {

    @Test
    void createSession_shouldCreateActiveSession() {
        UUID matchingRequestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        InMemorySessionRepository repository = new InMemorySessionRepository();
        SessionService service = new SessionService(repository);

        SkillSession session = service.createSession(matchingRequestId, requesterId, receiverId);

        assertNotNull(session.getId());
        assertEquals(matchingRequestId, session.getMatchingRequestId());
        assertEquals(requesterId, session.getRequesterUserId());
        assertEquals(receiverId, session.getReceiverUserId());
        assertEquals(SessionStatus.ACTIVE, session.getStatus());
        assertTrue(session.hasParticipant(requesterId));
        assertTrue(session.hasParticipant(receiverId));
    }

    @Test
    void createSession_shouldThrow_whenSameUserIsUsedTwice() {
        UUID matchingRequestId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        InMemorySessionRepository repository = new InMemorySessionRepository();
        SessionService service = new SessionService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.createSession(matchingRequestId, userId, userId)
        );
    }

    @Test
    void createSession_shouldThrow_whenActiveSessionAlreadyExistsBetweenUsers() {
        UUID firstMatchingRequestId = UUID.randomUUID();
        UUID secondMatchingRequestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        InMemorySessionRepository repository = new InMemorySessionRepository();
        SessionService service = new SessionService(repository);

        service.createSession(firstMatchingRequestId, requesterId, receiverId);

        assertThrows(
                IllegalStateException.class,
                () -> service.createSession(secondMatchingRequestId, requesterId, receiverId)
        );
    }

    @Test
    void createSession_shouldThrow_whenSessionAlreadyExistsForMatchingRequest() {
        UUID matchingRequestId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        InMemorySessionRepository repository = new InMemorySessionRepository();
        SessionService service = new SessionService(repository);

        service.createSession(matchingRequestId, requesterId, receiverId);

        assertThrows(
                IllegalStateException.class,
                () -> service.createSession(matchingRequestId, UUID.randomUUID(), UUID.randomUUID())
        );
    }

    private static class InMemorySessionRepository implements ISessionRepositoryPort {

        private SkillSession savedSession;

        @Override
        public SkillSession save(SkillSession session) {
            this.savedSession = session;
            return session;
        }

        @Override
        public Optional<SkillSession> findById(UUID sessionId) {
            if (savedSession != null && savedSession.getId().equals(sessionId)) {
                return Optional.of(savedSession);
            }

            return Optional.empty();
        }

        @Override
        public boolean existsActiveSessionBetween(UUID requesterUserId, UUID receiverUserId) {
            if (savedSession == null) {
                return false;
            }

            return savedSession.getStatus() == SessionStatus.ACTIVE
                    && savedSession.isBetween(requesterUserId, receiverUserId);
        }

        @Override
        public boolean existsByMatchingRequestId(UUID matchingRequestId) {
            return savedSession != null
                    && savedSession.getMatchingRequestId().equals(matchingRequestId);
        }

        @Override
        public Optional<SkillSession> findByMatchingRequestId(UUID matchingRequestId) {
            if (savedSession != null && savedSession.getMatchingRequestId().equals(matchingRequestId)) {
                return Optional.of(savedSession);
            }

            return Optional.empty();
        }
    }
}