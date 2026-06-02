package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.SkillSession;

import java.util.Optional;
import java.util.UUID;

public interface IGetSessionUseCase {

    Optional<SkillSession> findById(UUID sessionId);

    boolean isParticipant(UUID sessionId, UUID userId);

    boolean areParticipants(UUID sessionId, UUID firstUserId, UUID secondUserId);
}
