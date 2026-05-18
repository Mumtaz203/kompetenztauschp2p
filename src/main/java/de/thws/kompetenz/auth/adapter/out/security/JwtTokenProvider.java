package de.thws.kompetenz.auth.adapter.out.security;

import de.thws.kompetenz.auth.application.port.out.ITokenProviderPort;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtTokenProvider implements ITokenProviderPort {

    @Override
    public String generateToken(UUID userId, String email) {
        return Jwt.issuer("kompetenz-app") //who issued ID, can be changed in application.properties
                .subject(String.valueOf(userId)) // person id
                //.claim("email", email) // extra info in the jwt token
                .upn(email) // almost the same as .claim but more standart and with it we can use the email as user/principal name (grants easier access)
                .groups(Set.of("USER")) //add admin for the future.
                .expiresIn(Duration.ofHours(1))
                .sign(); // uses the privateKey.pem
    }

}
