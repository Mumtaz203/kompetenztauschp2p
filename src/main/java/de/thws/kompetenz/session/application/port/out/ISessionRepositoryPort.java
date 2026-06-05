package de.thws.kompetenz.session.application.port.out;

import de.thws.kompetenz.session.domain.SkillSession;

import java.util.Optional;
import java.util.UUID;

public interface ISessionRepositoryPort {

    SkillSession save(SkillSession session);

    Optional<SkillSession> findById(UUID sessionId);

    boolean existsActiveSessionBetween(UUID requesterUserId, UUID receiverUserId);

    boolean existsByMatchingRequestId(UUID matchingRequestId);

}
