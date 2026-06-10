package de.thws.kompetenz.rating.adapter.out.persistence.entity;

import de.thws.kompetenz.rating.domain.RatingStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "session_ratings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"session_id", "sender_user_id"})
        }
)
public class SessionRatingEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "UUID")
    private UUID sessionId;

    @Column(name = "sender_user_id", nullable = false, columnDefinition = "UUID")
    private UUID senderUserId;

    @Column(name = "receiver_user_id", nullable = false, columnDefinition = "UUID")
    private UUID receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RatingStatus status;

    @Column(name = "points", nullable = false, precision = 2, scale = 1)
    private BigDecimal points;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public SessionRatingEntity() {
    }

    public SessionRatingEntity(UUID id, UUID sessionId, UUID senderUserId, UUID receiverUserId,
                               RatingStatus status, BigDecimal points, String comment,
                               LocalDateTime createdAt, LocalDateTime publishedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.status = status;
        this.points = points;
        this.comment = comment;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
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

    public void setId(UUID id) {
        this.id = id;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public void setSenderUserId(UUID senderUserId) {
        this.senderUserId = senderUserId;
    }

    public void setReceiverUserId(UUID receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public void setStatus(RatingStatus status) {
        this.status = status;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}