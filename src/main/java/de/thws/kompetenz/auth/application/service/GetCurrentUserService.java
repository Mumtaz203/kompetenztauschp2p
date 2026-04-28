package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.port.in.IGetCurrentUserUseCase;
import de.thws.kompetenz.auth.domain.model.AuthenticatedUser;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class GetCurrentUserService implements IGetCurrentUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetCurrentUserService(UserRepositoryPort userRepositoryPort){
        this.userRepositoryPort = userRepositoryPort;
    }

    public User getCurrentUser(UUID id){

        return userRepositoryPort.findUserById(id).orElseThrow(() -> new RuntimeException("No user with this id"));
    }
}
