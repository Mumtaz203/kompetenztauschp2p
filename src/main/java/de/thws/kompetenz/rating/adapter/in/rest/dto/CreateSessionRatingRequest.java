package de.thws.kompetenz.rating.adapter.in.rest.dto;

import io.smallrye.common.constraint.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSessionRatingRequest(
        @NotNull
        UUID sessionId,

        @NotNull
        UUID receiverUserId,

        @NotNull
        @DecimalMin("0")
        @DecimalMax("5")
        BigDecimal points,

        @Size(max = 500)
        String comment
) {
}
