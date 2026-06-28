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
        if (sessionId == null) {
            throw new IllegalArgumentException("Session id must not be null");
        }
        if (reporterUserId == null || reportedUserId == null) {
            throw new IllegalArgumentException("Report users must not be null");
        }
        if (reporterUserId.equals(reportedUserId)) {
            throw new IllegalArgumentException("A user cannot report himself");
        }
        if (reasonCode == null) {
            throw new IllegalArgumentException("Report reason must not be null");
        }

        this.id = id;
        this.sessionId = sessionId;
        this.reporterUserId = reporterUserId;
        this.reportedUserId = reportedUserId;
        this.reasonCode = reasonCode;
        this.description = normalizeDescription(description);
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public static PrivateSessionReport create(UUID sessionId, UUID reporterUserId, UUID reportedUserId,
                                              PrivateSessionReportReason reasonCode, String description) {
        return new PrivateSessionReport(
                java.util.UUID.randomUUID(),
                sessionId,
                reporterUserId,
                reportedUserId,
                reasonCode,
                description,
                LocalDateTime.now()
        );
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
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


