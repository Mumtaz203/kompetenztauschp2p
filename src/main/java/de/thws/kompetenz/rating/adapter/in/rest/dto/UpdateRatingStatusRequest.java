package de.thws.kompetenz.rating.adapter.in.rest.dto;

import de.thws.kompetenz.rating.domain.RatingStatus;

public record UpdateRatingStatusRequest(
        RatingStatus status
) {
}
