package de.thws.kompetenz.auth.application.port.in;

import de.thws.kompetenz.auth.domain.model.User;

public interface RegisterUseCase {

    User register(String username, String email, String password);
}
