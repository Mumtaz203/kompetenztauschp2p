package de.thws.kompetenz.session.adapter.out.persistence.entity;

import de.thws.kompetenz.session.domain.PrivateSessionReportReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "private_session_report",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_private_session_report_once_per_pair",
                columnNames = {"session_id", "reporter_user_id", "reported_user_id"}
        )
)
public class PrivateSessionReportEntity {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "session_id", nullable = false)
    public UUID sessionId;

    @Column(name = "reporter_user_id", nullable = false)
    public UUID reporterUserId;

    @Column(name = "reported_user_id", nullable = false)
    public UUID reportedUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    public PrivateSessionReportReason reasonCode;

    @Column(name = "description", length = 2000)
    public String description;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
