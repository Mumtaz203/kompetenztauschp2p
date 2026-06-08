package de.thws.kompetenz.rating.application.in;

import de.thws.kompetenz.rating.domain.SessionRating;

import java.util.List;
import java.util.UUID;

public interface IPublishSessionRatingsUseCase {

    List<SessionRating> publishRatingsForSession(UUID sessionId);
}