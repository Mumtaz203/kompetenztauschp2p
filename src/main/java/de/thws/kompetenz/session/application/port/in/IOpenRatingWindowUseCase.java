package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.SkillSession;

import java.time.LocalDateTime;
import java.util.UUID;

public interface IOpenRatingWindowUseCase {
    SkillSession openRatingWindow(UUID sessionId, LocalDateTime ratingWindowEndsAt);

}
