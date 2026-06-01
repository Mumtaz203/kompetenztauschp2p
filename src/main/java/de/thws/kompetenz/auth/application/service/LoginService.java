package de.thws.kompetenz.auth.application.service;

import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.out.ITokenProviderPort;
import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.user.domain.model.exception.InvalidCredentialsException;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;

@ApplicationScoped
public class LoginService implements ILoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final IPasswordHasherPort passwordHasherPort;
    private final ITokenProviderPort tokenProvider;

    public LoginService(UserRepositoryPort userRepositoryPort, IPasswordHasherPort passwordHasherPort,
                        ITokenProviderPort tokenProvider){
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.tokenProvider = tokenProvider;
    }



    @Override
    public String login(LoginCommand loginCommand){

        String normalizedEmail = loginCommand.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepositoryPort.findByEmail(normalizedEmail).orElseThrow(
                () -> new InvalidCredentialsException());

        boolean matches = passwordHasherPort.verify(loginCommand.password(), user.getPassword());

        if(!matches){
            throw new InvalidCredentialsException();
        }

        return tokenProvider.generateToken(user.getId(), user.getEmail());
    }
}
