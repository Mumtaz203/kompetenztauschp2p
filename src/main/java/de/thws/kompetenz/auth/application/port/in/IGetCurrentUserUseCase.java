package de.thws.kompetenz.auth.application.port.in;

import de.thws.kompetenz.user.domain.model.User;

import java.util.UUID;

public interface IGetCurrentUserUseCase {

    User getCurrentUser(UUID id);
}
