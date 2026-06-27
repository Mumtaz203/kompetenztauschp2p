package de.thws.kompetenz.user.adapter.in.rest.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateInternalFlagRequest(
        @NotNull Boolean internallyFlagged
) {
}
