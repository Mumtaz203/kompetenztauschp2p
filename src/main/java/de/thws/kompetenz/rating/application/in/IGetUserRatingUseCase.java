package de.thws.kompetenz.rating.application.in;

import de.thws.kompetenz.rating.domain.SessionRating;

import java.util.List;
import java.util.UUID;

public interface IGetUserRatingUseCase {
    SessionRating getRating(UUID ratingId);
    SessionRating getVisibleRating(UUID ratingId, UUID currentUserId, boolean isAdmin);

    SessionRating getPublishedRating(UUID ratingId);

    SessionRating getNonPublishedRating(UUID ratingId);

    List<SessionRating> getAllRatings();

    List<SessionRating> getAllPublishedRatings();

    List<SessionRating> getAllNonPublishedRatings();

    List<SessionRating> getVisibleRatingsForUser(UUID currentUserId);

    List<SessionRating> getOwnRatings(UUID currentUserId);

    List<SessionRating> getPublishedRatingsForUser(UUID userId);

    List<SessionRating> getAllRatingsForUser(UUID userId);
}
