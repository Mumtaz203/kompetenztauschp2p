package de.thws.kompetenz.session.application.port.out;

import de.thws.kompetenz.session.domain.SkillSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ISessionRepositoryPort {

    SkillSession save(SkillSession session);

    Optional<SkillSession> findById(UUID sessionId);

    List<SkillSession>  getAll();

    boolean existsActiveSessionBetween(UUID requesterUserId, UUID receiverUserId);

    boolean existsByMatchingRequestId(UUID matchingRequestId);

    Optional<SkillSession> findByMatchingRequestId(UUID matchingRequestId);

    List<SkillSession> findRatingOpenSessionsWithExpiredWindow(LocalDateTime now);

    List<SkillSession> findActiveSessionsAcceptedBefore(LocalDateTime cutoff);

    List<SkillSession> findCompletionPendingSessionsWithStaleResponses(LocalDateTime cutoff);

}
