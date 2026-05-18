package de.thws.kompetenz.auth.application.result;

import de.thws.kompetenz.user.domain.model.User;

public record RegisterResult(
        User user,
        String token
) {
}
