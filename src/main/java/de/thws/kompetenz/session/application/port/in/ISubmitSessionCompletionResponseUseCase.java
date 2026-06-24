package de.thws.kompetenz.session.application.port.in;

import de.thws.kompetenz.session.domain.SessionCompletionAnswer;
import de.thws.kompetenz.session.domain.SkillSession;

import java.util.UUID;

public interface ISubmitSessionCompletionResponseUseCase {
    SkillSession submitCompletionResponse(
            UUID sessionId,
            UUID userId,
            SessionCompletionAnswer answer,
            String reason
    );
}
