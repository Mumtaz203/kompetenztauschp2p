package de.thws.kompetenz.session.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class SessionCompletionResponse {
    UUID id;
    UUID sessionId;
    UUID userId;
    SessionCompletionAnswer answer;
    String reason;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public SessionCompletionResponse(UUID id, UUID sessionId, UUID userId, SessionCompletionAnswer answer, String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {

        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        if (userId == null) {
            throw new IllegalArgumentException("User id must not be null");
        }

        if (answer == null) {
            throw new IllegalArgumentException("Completion answer must not be null");
        }

        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.answer = answer;
        this.reason = normalizeReason(reason);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static SessionCompletionResponse create(UUID sessionId, UUID userId, SessionCompletionAnswer answer,
                                            String reason){
        return new SessionCompletionResponse(
                UUID.randomUUID(),
                sessionId,
                userId,
                answer,
                reason,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public void updateAnswer(SessionCompletionAnswer answer, String reason) {
        if (answer == null) {
            throw new IllegalArgumentException("Completion answer must not be null");
        }

        this.answer = answer;
        this.reason = normalizeReason(reason);
        this.updatedAt = LocalDateTime.now();
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }

        return reason.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public SessionCompletionAnswer getAnswer() {
        return answer;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
