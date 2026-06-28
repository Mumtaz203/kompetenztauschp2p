package de.thws.kompetenz.session.application.port.out;

import de.thws.kompetenz.session.domain.SessionCompletionResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ISessionCompletionResponseRepositoryPort {

    SessionCompletionResponse save(SessionCompletionResponse response);

    Optional<SessionCompletionResponse> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    List<SessionCompletionResponse> findBySessionId(UUID sessionId);
}
