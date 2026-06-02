package de.thws.kompetenz.session.adapter.out.persistence.entity;

import de.thws.kompetenz.session.domain.SessionStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "skill_session")
public class SkillSessionEntity {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "requester_user_id", nullable = false)
    public UUID requesterUserId;

    @Column(name = "receiver_user_id", nullable = false)
    public UUID receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    public SessionStatus status;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "accepted_at", nullable = false)
    public LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @Column(name = "rating_window_opened_at")
    public LocalDateTime ratingWindowOpenedAt;

    @Column(name = "rating_window_ends_at")
    public LocalDateTime ratingWindowEndsAt;
}