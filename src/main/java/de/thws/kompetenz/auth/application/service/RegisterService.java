package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.command.RegisterCommand;
import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.auth.application.port.out.ITokenProviderPort;
import de.thws.kompetenz.auth.application.result.RegisterResult;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.user.domain.model.exception.EmailAlreadyExistsException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Locale;
import java.util.Set;

@ApplicationScoped
public class RegisterService implements RegisterUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final IPasswordHasherPort passwordHasher;

    @Inject
    ITokenProviderPort tokenProviderPort;


    public RegisterService(UserRepositoryPort userRepositoryPort, IPasswordHasherPort passwordHasher) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasher = passwordHasher;
    }

    @Transactional //to ensure data persistency again
    @Override
    public RegisterResult register(RegisterCommand command) {

        String normalizedUsername = command.username().trim();

        String normalizedEmail = command.email().trim().toLowerCase(Locale.ROOT);

        if (userRepositoryPort.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        String hashedPassword = passwordHasher.hash(command.password());

        User user = new User(normalizedUsername, normalizedEmail, hashedPassword);

        User savedUser = userRepositoryPort.save(user);

        String token = tokenProviderPort.generateToken(savedUser.getId(), savedUser.getEmail(), Set.of("USER"));

        return new RegisterResult(savedUser, token);
    }
}
