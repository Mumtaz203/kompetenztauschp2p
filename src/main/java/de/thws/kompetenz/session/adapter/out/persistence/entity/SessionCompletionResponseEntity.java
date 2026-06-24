package de.thws.kompetenz.session.adapter.out.persistence.entity;

import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "session_completion_response",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_session_completion_response_session_user",
                        columnNames = {"session_id", "user_id"}
                )
        }
)
public class SessionCompletionResponseEntity {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "session_id", nullable = false)
    public UUID sessionId;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer", nullable = false)
    public SessionCompletionAnswer answer;

    @Column(name = "reason", length = 1000)
    public String reason;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;
}