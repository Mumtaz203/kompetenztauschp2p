package de.thws.kompetenz.session.application.port.in;


import de.thws.kompetenz.session.domain.SkillSession;

import java.util.UUID;

public interface ICreateSessionUseCase {

    SkillSession createSession(UUID requesterUserId, UUID receiverUserId);
}
