package de.thws.kompetenz.rating.adapter.out.persistence.entity;

import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="sessionRating",
                        uniqueConstraints = {@UniqueConstraint(columnNames = {"sessionID", "senderUserID"}
                        )
})

public class SessionRatingEntity {

    @Id
    @UuidGenerator
    UUID id;

    @Column(name="sessionID", nullable = false, columnDefinition = "UUID")
    UUID sessionId;

    @Column(name="senderUserID", nullable = false, columnDefinition = "UUID")
    UUID senderUserId;

    @Column(name="receiverUserID", nullable = false, columnDefinition = "UUID")
    UUID receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    RatingStatus status;

    @Column(name = "points", nullable = false, precision = 2, scale = 1)
    BigDecimal points;

    @Column(name="comment", length = 1000)
    String comment;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "published_at")
    LocalDateTime publishedAt;

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

    public SessionRatingEntity() {

    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getReceiverUserId() {
        return senderUserId;
    }

    public void setSenderUserId(UUID senderUserId) {
        this.senderUserId = senderUserId;
    }

    public UUID getSenderUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(UUID receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public RatingStatus getStatus() {
        return status;
    }

    public void setStatus(RatingStatus status) {
        this.status = status;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}
