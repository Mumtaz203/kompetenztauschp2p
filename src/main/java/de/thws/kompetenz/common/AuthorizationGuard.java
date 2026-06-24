package de.thws.kompetenz.common;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.ForbiddenException;


import java.util.UUID;

@ApplicationScoped
public class AuthorizationGuard {

    @Inject
    JsonWebToken jwt;

    public UUID currentUserId(){
        return UUID.fromString(jwt.getSubject());
    }

    public boolean isAdmin(){
        return jwt.getGroups().contains("ADMIN");
    }
    public void requireSelfOrAdmin(UUID userId){
        if(isAdmin()){
            return;
        }
        if(!currentUserId().equals(userId)){
            throw new ForbiddenException("You are not allowed to access this resource.");
        }
    }

    public void requireParticipantOrAdmin(boolean isParticipant){
        if(isAdmin()){
            return;
        }
        if(!isParticipant){
            throw new ForbiddenException("You are not allowed to access this resource.");
        }
    }

    public void requireAdmin(){
        if(isAdmin()){
            return;
        }
        throw new ForbiddenException("You are not allowed to access this resource.");
    }

    public boolean isCurrentUser(UUID userId) {
        return currentUserId().equals(userId);
    }
}