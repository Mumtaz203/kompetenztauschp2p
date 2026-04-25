package de.thws.kompetenz.auth.adapter.out.security;

import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.elytron.security.common.BcryptUtil;

@ApplicationScoped
public class BCryptIPasswordHasher implements IPasswordHasherPort {

    @Override
    public String hash(String plainPassword) {
        return BcryptUtil.bcryptHash(plainPassword);
    }

    @Override
    public boolean verify(String plainPassword, String hashedPassword) {
        return BcryptUtil.matches(plainPassword, hashedPassword);
    }
}