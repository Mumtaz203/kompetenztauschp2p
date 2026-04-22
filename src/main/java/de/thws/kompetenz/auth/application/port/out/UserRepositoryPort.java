package de.thws.kompetenz.auth.application.port.out;

import de.thws.kompetenz.auth.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
