package de.thws.kompetenz.auth.application.port.in;


import de.thws.kompetenz.auth.application.command.RegisterCommand;
import de.thws.kompetenz.auth.application.result.RegisterResult;

public interface RegisterUseCase {

    RegisterResult register(RegisterCommand registerCommand);
}
