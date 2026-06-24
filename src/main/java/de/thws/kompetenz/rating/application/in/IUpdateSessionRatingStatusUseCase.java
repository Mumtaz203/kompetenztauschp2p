package de.thws.kompetenz.rating.application.in;

import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.SessionRating;

import java.util.UUID;

public interface IUpdateSessionRatingStatusUseCase {

    SessionRating updateRatingStatus(UUID ratingId, RatingStatus newStatus);

}
