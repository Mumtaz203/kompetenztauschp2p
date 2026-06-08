package de.thws.kompetenz.rating.domain;

import java.math.BigDecimal;

public record RatingSummary(
        BigDecimal averagePoints,
        long ratingCount
) {
}
