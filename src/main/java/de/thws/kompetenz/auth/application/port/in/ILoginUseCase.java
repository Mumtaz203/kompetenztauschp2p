package de.thws.kompetenz.auth.application.port.in;


public interface ILoginUseCase {

    String login(String email, String password);
}
