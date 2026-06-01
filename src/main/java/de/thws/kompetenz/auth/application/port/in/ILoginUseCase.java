package de.thws.kompetenz.auth.application.port.in;


import de.thws.kompetenz.auth.application.command.LoginCommand;

public interface ILoginUseCase {

    String login(LoginCommand loginCommand);
}
