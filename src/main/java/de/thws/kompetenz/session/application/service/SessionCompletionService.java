package de.thws.kompetenz.session.application.service;

import de.thws.kompetenz.session.application.port.in.ISubmitSessionCompletionResponseUseCase;
import de.thws.kompetenz.session.application.port.out.ISessionCompletionResponseRepositoryPort;
import de.thws.kompetenz.session.application.port.out.ISessionRepositoryPort;
import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import de.thws.kompetenz.session.domain.SessionCompletionResponse;
import de.thws.kompetenz.session.domain.SessionStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SessionCompletionService implements ISubmitSessionCompletionResponseUseCase {
    private static final int RATING_WINDOW_DAYS = 7;

    private final ISessionRepositoryPort sessionRepositoryPort;

    private final ISessionCompletionResponseRepositoryPort sessionCompletionResponseRepositoryPort;

    public SessionCompletionService(
            ISessionRepositoryPort sessionRepositoryPort,
            ISessionCompletionResponseRepositoryPort sessionCompletionResponseRepositoryPort
    ) {
        this.sessionRepositoryPort = sessionRepositoryPort;
        this.sessionCompletionResponseRepositoryPort = sessionCompletionResponseRepositoryPort;
    }

    @Override
    @Transactional
    public SkillSession submitCompletionResponse(UUID sessionId, UUID userId, SessionCompletionAnswer answer, String reason){

        if( sessionId == null || userId == null || answer == null){
            throw new IllegalArgumentException("Session id, user id and answer cannot be null");
        }

        SkillSession session = sessionRepositoryPort.findById(sessionId).orElseThrow(() -> new IllegalArgumentException("Session not found by this id: " + sessionId));

        if(!session.hasParticipant(userId)){
            throw new IllegalArgumentException("User is not a participant of this session!");
        }
        if(session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.COMPLETION_CONFIRMATION_PENDING){
            throw new IllegalStateException("Completion response is only for active or pending sessions");
        }

        Optional<SessionCompletionResponse> existingResponse = sessionCompletionResponseRepositoryPort.findBySessionIdAndUserId(sessionId, userId);

        SessionCompletionResponse response;

        if(existingResponse.isPresent()){
            response = existingResponse.get();
            response.updateAnswer(answer,reason);
        }else{
            response = SessionCompletionResponse.create(sessionId,
                    userId,
                    answer,
                    reason);
        }

        sessionCompletionResponseRepositoryPort.save(response);

        List<SessionCompletionResponse> responses = sessionCompletionResponseRepositoryPort.findBySessionId(sessionId);

        applyCompletionLogic(session, responses);

        return sessionRepositoryPort.save(session);
    }

    private void applyCompletionLogic(SkillSession session,
                                      List<SessionCompletionResponse> responses){

        LocalDateTime now = LocalDateTime.now();

        boolean hasCompleted = responses.stream()
                .anyMatch(response -> response.getAnswer() == SessionCompletionAnswer.COMPLETED);

        boolean hasNotYet = responses.stream()
                .anyMatch(response -> response.getAnswer() == SessionCompletionAnswer.NOT_YET);

        boolean hasCancelled = responses.stream()
                .anyMatch(response -> response.getAnswer() == SessionCompletionAnswer.CANCELLED);

        boolean hasProblem = responses.stream()
                .anyMatch(response -> response.getAnswer() == SessionCompletionAnswer.PROBLEM);

        if (hasProblem) {
            session.markDisputed();
            return;
        }

        if (responses.size() < 2) {
            session.markCompletionConfirmationPending();
            return;
        }

        if (hasCompleted && !hasNotYet && !hasCancelled) {
            session.completeAndOpenRatingWindow(
                    now,
                    now,
                    now.plusDays(RATING_WINDOW_DAYS)
            );
            return;
        }

        if (hasNotYet && !hasCompleted && !hasCancelled) {
            session.markStillPlanned();
            return;
        }

        if (hasCancelled && !hasCompleted && !hasNotYet) {
            session.cancelSession();
            return;
        }

        if (hasCompleted && hasNotYet) {
            session.markCompletionConfirmationPending();
            return;
        }

        if (hasCompleted && hasCancelled) {
            session.markDisputed();
            return;
        }

        if (hasNotYet && hasCancelled) {
            session.markDisputed();
            return;
        }

        session.markDisputed();
    }


}
