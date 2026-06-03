package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.out.ITokenProviderPort;
import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import de.thws.kompetenz.auth.application.result.LoginResult;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.user.domain.model.exception.InvalidCredentialsException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class LoginService implements ILoginUseCase {

    @ConfigProperty(name = "app.admin.id", defaultValue = "00000000-0000-0000-0000-000000000001")
    String adminId;

    @ConfigProperty(name = "app.admin.email")
    String adminEmail;

    @ConfigProperty(name = "app.admin.password")
    String adminPassword;

    private final UserRepositoryPort userRepositoryPort;
    private final IPasswordHasherPort passwordHasherPort;
    private final ITokenProviderPort tokenProvider;

    public LoginService(UserRepositoryPort userRepositoryPort, IPasswordHasherPort passwordHasherPort,
                        ITokenProviderPort tokenProvider) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public LoginResult login(LoginCommand loginCommand) {
        String normalizedEmail = loginCommand.email().trim().toLowerCase(Locale.ROOT);

        String normalizedAdminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        boolean isAdminCredentials = normalizedEmail.equals(normalizedAdminEmail)
                && loginCommand.password().equals(adminPassword);

        if (isAdminCredentials) {
            UUID adminUuid = UUID.fromString(adminId);
            String token = tokenProvider.generateToken(adminUuid, adminEmail, Set.of("ADMIN"));
            return new LoginResult(token, "ADMIN");
        }

        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordHasherPort.verify(loginCommand.password(), user.getPassword());
        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), Set.of("USER"));
        return new LoginResult(token, "USER");
    }
}
