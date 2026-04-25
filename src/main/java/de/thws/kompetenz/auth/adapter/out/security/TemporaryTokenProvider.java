package de.thws.kompetenz.auth.adapter.out.security;

import de.thws.kompetenz.auth.application.port.in.ITokenProviderPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class TemporaryTokenProvider implements ITokenProviderPort {

    @Override
    public String generateToken(UUID userId, String email) {
        return "temp-token-" + userId + "-" + email;
    }
}
