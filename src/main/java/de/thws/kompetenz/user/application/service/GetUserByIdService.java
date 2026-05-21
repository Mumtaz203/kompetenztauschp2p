package de.thws.kompetenz.user.application.service;

import de.thws.kompetenz.user.application.port.in.IGetUserByIdUseCase;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class GetUserByIdService implements IGetUserByIdUseCase {
    private final UserRepositoryPort userRepositoryPort;

    public GetUserByIdService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> getUserById(UUID userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return userRepositoryPort.findUserById(userId);
    }
}
