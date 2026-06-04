package de.thws.kompetenz.auth.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface ITokenProviderPort {


    String generateToken(UUID userId, String email, Set<String> groups);

}
