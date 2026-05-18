package de.thws.kompetenz.auth.application.command;

public record RegisterCommand(
        String username,
        String email,
        String password
) {
}
