package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;


import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;


@ApplicationScoped
public class SearchUserService implements SearchUserUseCase {
    private final UserRepositoryPort userRepositoryPort;

    public SearchUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public List<User> searchBySkill(String skill) {
      return userRepositoryPort.searchBySkill(skill);
    }
}
