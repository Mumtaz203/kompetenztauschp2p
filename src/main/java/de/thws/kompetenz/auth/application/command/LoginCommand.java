package de.thws.kompetenz.auth.application.command;

public record LoginCommand(
        String email,
        String password
) {
}
