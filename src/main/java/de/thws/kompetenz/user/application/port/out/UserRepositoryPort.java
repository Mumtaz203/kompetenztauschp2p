package de.thws.kompetenz.user.application.port.out;

import de.thws.kompetenz.user.domain.model.User;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

     List<User> searchBySkill(String skill);

    Optional<User>findUserById(UUID userId);
}
