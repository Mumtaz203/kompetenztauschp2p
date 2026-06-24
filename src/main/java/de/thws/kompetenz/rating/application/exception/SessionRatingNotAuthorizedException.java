package de.thws.kompetenz.rating.application.exception;

public class SessionRatingNotAuthorizedException extends RuntimeException{

    public SessionRatingNotAuthorizedException(){
        super("Not published ratings are not visible for users");
    }
}
