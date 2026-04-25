package de.thws.kompetenz.user.domain.model.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(){
        super("Invalid Credentials!");
    }
}
