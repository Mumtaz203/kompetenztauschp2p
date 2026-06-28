package de.thws.kompetenz.session.adapter.in.rest.dto;

import de.thws.kompetenz.session.domain.PrivateSessionReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePrivateSessionReportRequest(
        @NotNull UUID reportedUserId,
        @NotNull PrivateSessionReportReason reasonCode,
        @Size(max = 2000) String description
) {
}
