package de.thws.kompetenz.session.adapter.in.rest.dto;

import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import jakarta.validation.constraints.NotNull;

public record SubmitSessionCompletionResponseRequest(
        @NotNull SessionCompletionAnswer answer,
        String reason
        ) {
}
