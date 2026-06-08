package de.thws.kompetenz.rating.adapter.in.rest.dto;

import java.math.BigDecimal;

public record RatingSummaryResponce(
        BigDecimal averagePoints,
        long ratingCount
) {
}
