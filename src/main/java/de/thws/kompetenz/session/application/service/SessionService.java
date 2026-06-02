package de.thws.kompetenz.session.application.service;


import de.thws.kompetenz.session.application.port.in.ICreateSessionUseCase;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionService implements ICreateSessionUseCase, IGetSessionUseCase {

    private final ISessionRepositoryPort sessionRepositoryPort;

    public SessionService(ISessionRepositoryPort sessionRepositoryPort) {
        this.sessionRepositoryPort = sessionRepositoryPort;
    }

    @Override
    @Transactional
    public SkillSession createSession(UUID requesterUserId, UUID receiverUserId) {
        if (requesterUserId == null || receiverUserId == null) {
            throw new IllegalArgumentException("User ids must not be null");
        }

        if (requesterUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("A user cannot create a session with himself");
        }

        if (sessionRepositoryPort.existsActiveSessionBetween(requesterUserId, receiverUserId)) {
            throw new IllegalStateException("There is already an active session between these users");
        }

        SkillSession session = SkillSession.create(requesterUserId, receiverUserId);
        return sessionRepositoryPort.save(session);
    }

    @Override
    public Optional<SkillSession> findById(UUID sessionId) {
        return sessionRepositoryPort.findById(sessionId);
    }

    @Override
    public boolean isParticipant(UUID sessionId, UUID userId) {
        return sessionRepositoryPort.findById(sessionId)
                .map(session -> session.hasParticipant(userId))
                .orElse(false);
    }

    @Override
    public boolean areParticipants(UUID sessionId, UUID firstUserId, UUID secondUserId) {
        return sessionRepositoryPort.findById(sessionId)
                .map(session -> session.hasParticipants(firstUserId, secondUserId))
                .orElse(false);
    }
}
