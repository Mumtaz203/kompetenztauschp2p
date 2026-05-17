package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Override
    public List<User> searchBySkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        if (skills.size() == 1) {
            return userRepositoryPort.searchBySkill(skills.getFirst());
        }

        Map<UUID, User> usersById = new LinkedHashMap<>();
        for (String term : skills) {
            for (User user : userRepositoryPort.searchBySkill(term)) {
                usersById.putIfAbsent(user.getId(), user);
            }
        }
        return new ArrayList<>(usersById.values());
    }
}
