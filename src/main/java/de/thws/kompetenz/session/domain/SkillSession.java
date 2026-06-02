package de.thws.kompetenz.session.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class SkillSession {

    private final UUID id;
    private final UUID requesterUserId;
    private final UUID receiverUserId;
    private SessionStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private LocalDateTime ratingWindowOpenedAt;
    private LocalDateTime ratingWindowEndsAt;

    public SkillSession(
            UUID id,
            UUID requesterUserId,
            UUID receiverUserId,
            SessionStatus status,
            LocalDateTime createdAt,
            LocalDateTime acceptedAt,
            LocalDateTime completedAt,
            LocalDateTime ratingWindowOpenedAt,
            LocalDateTime ratingWindowEndsAt
    ) {
        if (requesterUserId == null || receiverUserId == null) {
            throw new IllegalArgumentException("Session participants must not be null");
        }

        if (requesterUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("A user cannot create a session with himself");
        }

        this.id = id;
        this.requesterUserId = requesterUserId;
        this.receiverUserId = receiverUserId;
        this.status = status == null ? SessionStatus.ACTIVE : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.acceptedAt = acceptedAt == null ? LocalDateTime.now() : acceptedAt;
        this.completedAt = completedAt;
        this.ratingWindowOpenedAt = ratingWindowOpenedAt;
        this.ratingWindowEndsAt = ratingWindowEndsAt;
    }

    public static SkillSession create(UUID requesterUserId, UUID receiverUserId) {
        return new SkillSession(
                UUID.randomUUID(),
                requesterUserId,
                receiverUserId,
                SessionStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    public boolean hasParticipant(UUID userId) {
        return Objects.equals(requesterUserId, userId)
                || Objects.equals(receiverUserId, userId);
    }

    public boolean hasParticipants(UUID firstUserId, UUID secondUserId) {
        return hasParticipant(firstUserId) && hasParticipant(secondUserId);
    }

    public boolean isBetween(UUID firstUserId, UUID secondUserId) {
        return (Objects.equals(requesterUserId, firstUserId) && Objects.equals(receiverUserId, secondUserId))
                || (Objects.equals(requesterUserId, secondUserId) && Objects.equals(receiverUserId, firstUserId));
    }

    public UUID getOtherParticipant(UUID userId) {
        if (Objects.equals(requesterUserId, userId)) {
            return receiverUserId;
        }

        if (Objects.equals(receiverUserId, userId)) {
            return requesterUserId;
        }

        throw new IllegalArgumentException("User is not a participant of this session");
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequesterUserId() {
        return requesterUserId;
    }

    public UUID getReceiverUserId() {
        return receiverUserId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public LocalDateTime getRatingWindowOpenedAt() {
        return ratingWindowOpenedAt;
    }

    public LocalDateTime getRatingWindowEndsAt() {
        return ratingWindowEndsAt;
    }
}