package de.thws.kompetenz.user.domain.model.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(UUID id){
        super("User not found: " + id);
    }

    public UserNotFoundException(){

    }
}
