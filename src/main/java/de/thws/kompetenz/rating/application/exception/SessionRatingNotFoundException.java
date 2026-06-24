package de.thws.kompetenz.rating.application.exception;

import java.util.UUID;

public class SessionRatingNotFoundException  extends RuntimeException {

    public SessionRatingNotFoundException(UUID ratingId) {
        super("Session rating not found with id: " + ratingId);
    }
}
