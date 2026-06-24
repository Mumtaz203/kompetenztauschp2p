package de.thws.kompetenz.rating.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SessionRating {

    private final UUID id;
    private final UUID sessionId;
    private final UUID senderUserId;
    private final UUID receiverUserId;
    private RatingStatus status;
    private final BigDecimal points;
    private final String comment;
    private final LocalDateTime createdAt;
    private LocalDateTime publishedAt;

    public SessionRating(UUID id, UUID sessionId, UUID senderUserId, UUID receiverUserId,
                         RatingStatus status, BigDecimal points, String comment,
                         LocalDateTime createdAt, LocalDateTime publishedAt) {

        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }

        if (senderUserId == null || receiverUserId == null) {
            throw new IllegalArgumentException("Rating users must not be null");
        }

        if (senderUserId.equals(receiverUserId)) {
            throw new IllegalArgumentException("A user cannot rate himself");
        }

        if (points == null) {
            throw new IllegalArgumentException("Points must not be null");
        }

        if (points.compareTo(BigDecimal.ONE) < 0 || points.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new IllegalArgumentException("Points must be between 1 and 5");
        }

        BigDecimal multiplied = points.multiply(BigDecimal.valueOf(2));

        if (multiplied.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Points must be in 0.5 steps");
        }

        this.id = id;
        this.sessionId = sessionId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.status = status == null ? RatingStatus.PENDING : status;
        this.points = points;
        this.comment = comment;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.publishedAt = publishedAt;
    }

    public static SessionRating create(UUID sessionId,
                                       UUID senderUserId,
                                       UUID receiverUserId,
                                       BigDecimal points,
                                       String comment) {
        return new SessionRating(
                null,
                sessionId,
                senderUserId,
                receiverUserId,
                RatingStatus.PENDING,
                points,
                comment,
                LocalDateTime.now(),
                null
        );
    }

    public void publish(LocalDateTime publishedAt) {
        if (status != RatingStatus.PENDING) {
            throw new IllegalStateException("Only pending ratings can be published");
        }

        this.status = RatingStatus.PUBLISHED;
        this.publishedAt = publishedAt == null ? LocalDateTime.now() : publishedAt;
    }

    public void changeStatus(RatingStatus newStatus, LocalDateTime now) {
        this.status = newStatus;

        if (newStatus == RatingStatus.PUBLISHED) {
            this.publishedAt = now;
        } else {
            this.publishedAt = null;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getSenderUserId() {
        return senderUserId;
    }

    public UUID getReceiverUserId() {
        return receiverUserId;
    }

    public RatingStatus getStatus() {
        return status;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}