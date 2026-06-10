package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.SkillSession;

import java.util.UUID;

public interface ExpireRatingWindowForTestingUseCase {

    SkillSession expireRatingWindowForTesting(UUID sessionId);
}