package de.thws.kompetenz.auth.application.port.in;

import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterRequest;
import de.thws.kompetenz.user.domain.model.User;

public interface RegisterUseCase {

    User register(User user);
}
