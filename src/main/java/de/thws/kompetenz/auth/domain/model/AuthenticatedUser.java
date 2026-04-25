package de.thws.kompetenz.auth.domain.model;

import java.util.UUID;

public class AuthenticatedUser {

    private UUID userId;
    private String email;




    public AuthenticatedUser(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

}
