package de.thws.kompetenz.session.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class PrivateSessionReport {
    private final UUID id;
    private final UUID sessionId;
    private final UUID reporterUserId;
    private final UUID reportedUserId;
    private final PrivateSessionReportReason reasonCode;
    private final String description;
    private final LocalDateTime createdAt;

    public PrivateSessionReport(UUID id, UUID sessionId, UUID reporterUserId, UUID reportedUserId, PrivateSessionReportReason reasonCode, String description, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.reporterUserId = reporterUserId;
        this.reportedUserId = reportedUserId;
        this.reasonCode = reasonCode;
        this.description = description;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getReporterUserId() {
        return reporterUserId;
    }

    public UUID getReportedUserId() {
        return reportedUserId;
    }

    public PrivateSessionReportReason getReasonCode() {
        return reasonCode;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}


