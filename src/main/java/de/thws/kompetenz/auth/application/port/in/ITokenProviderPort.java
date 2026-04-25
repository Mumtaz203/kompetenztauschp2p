package de.thws.kompetenz.auth.application.port.in;

import java.util.UUID;

public interface ITokenProviderPort {


    String generateToken(UUID userId, String email);

}
