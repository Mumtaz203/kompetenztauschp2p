package de.thws.kompetenz.auth.application.port.in;

import de.thws.kompetenz.user.domain.model.User;

public interface ILoginUseCase {

    //change later the String to a token (jwt)
    String login(String email, String password);
}
