package de.thws.kompetenz.rating.application.in;

import de.thws.kompetenz.rating.domain.RatingSummary;

import java.util.UUID;

public interface IGetRatingSummaryUseCase {
    RatingSummary getRatingSummaryForUser(UUID userId);

}
