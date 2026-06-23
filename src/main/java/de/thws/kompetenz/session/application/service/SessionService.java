package de.thws.kompetenz.session.application.service;


import de.thws.kompetenz.session.application.port.in.*;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionService implements ICreateSessionUseCase, IGetSessionUseCase, IOpenRatingWindowUseCase
                                        , ICloseRatingWindowUseCase, ExpireRatingWindowForTestingUseCase{

    private final ISessionRepositoryPort sessionRepositoryPort;

    public SessionService(ISessionRepositoryPort sessionRepositoryPort) {
        this.sessionRepositoryPort = sessionRepositoryPort;
    }

    @Override
    @Transactional
    public SkillSession createSession(UUID matchingRequestId, UUID requesterUserId, UUID receiverUserId) {
        if (matchingRequestId == null) {
            throw new IllegalArgumentException("Matching request id must not be null");
        }

        if (requesterUserId == null || receiverUserId == null) {
            throw new IllegalArgumentException("User ids must not be null");
        }

        if (requesterUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("A user cannot create a session with himself");
        }

        if (sessionRepositoryPort.existsByMatchingRequestId(matchingRequestId)) {
            throw new IllegalStateException("A session already exists for this matching request");
        }

        if (sessionRepositoryPort.existsActiveSessionBetween(requesterUserId, receiverUserId)) {
            throw new IllegalStateException("An active session already exists between these users");
        }

        SkillSession session = SkillSession.create(matchingRequestId, requesterUserId, receiverUserId);

        return sessionRepositoryPort.save(session);
    }

    @Override
    public List<SkillSession>  getAll(){
        return sessionRepositoryPort.getAll();
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

    @Override
    @Transactional
    public SkillSession openRatingWindow(UUID sessionId, LocalDateTime ratingWindowEndsAt) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        SkillSession session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.openRatingWindow(LocalDateTime.now(), ratingWindowEndsAt);

        return sessionRepositoryPort.save(session);
    }

    @Override
    @Transactional
    public SkillSession closeRatingWindow(UUID sessionId){
        if( sessionId == null ){
            throw new IllegalArgumentException("Session ID must not be null!");
        }

        SkillSession session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session is not found!"));

        session.closeRatingWindow();



        return sessionRepositoryPort.save(session);

    }

    @Override
    public Optional<SkillSession> findByMatchingRequestId(UUID matchingRequestId) {
        if (matchingRequestId == null) {
            throw new IllegalArgumentException("Matching request id must not be null");
        }

        return sessionRepositoryPort.findByMatchingRequestId(matchingRequestId);
    }

    @Override
    @Transactional
    public SkillSession expireRatingWindowForTesting(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        SkillSession session = sessionRepositoryPort.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        session.expireRatingWindowForTesting();

        return sessionRepositoryPort.save(session);
    }


}
