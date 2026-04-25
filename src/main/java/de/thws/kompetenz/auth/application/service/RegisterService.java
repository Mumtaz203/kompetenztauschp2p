package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.user.domain.model.exception.EmailAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Locale;

@ApplicationScoped
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final IPasswordHasherPort passwordHasher;


    public RegisterService(UserRepositoryPort userRepositoryPort, IPasswordHasherPort passwordHasher) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasher = passwordHasher;
    }

    @Transactional //to ensure data persistency again
    @Override
    public User register(User user) {

        String normalizedUsername = user.getUsername().trim();
        String normalizedEmail = user.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hashedPassword = passwordHasher.hash(user.getPassword());

        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(hashedPassword);

        return userRepositoryPort.save(user);
    }
}
