package de.thws.kompetenz.auth.application.port.in;


import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.result.LoginResult;

public interface ILoginUseCase {

    LoginResult login(LoginCommand loginCommand); //  changed it from String to Record
    //necessery implementations are done in AuthResource
}
