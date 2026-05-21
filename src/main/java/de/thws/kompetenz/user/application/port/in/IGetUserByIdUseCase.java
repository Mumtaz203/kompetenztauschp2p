package de.thws.kompetenz.user.application.port.in;

import de.thws.kompetenz.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface IGetUserByIdUseCase {
    Optional<User> getUserById(UUID userId);
}
