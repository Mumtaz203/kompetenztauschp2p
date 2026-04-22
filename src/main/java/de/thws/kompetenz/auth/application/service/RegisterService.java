package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.exception.EmailAlreadyExistsException;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.auth.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.auth.domain.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;

@ApplicationScoped
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public RegisterService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User register(String username, String email, String password) {
        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hashedPassword = BcryptUtil.bcryptHash(password);
        User user = new User(normalizedUsername, normalizedEmail, hashedPassword);
        return userRepositoryPort.save(user);
    }
}
