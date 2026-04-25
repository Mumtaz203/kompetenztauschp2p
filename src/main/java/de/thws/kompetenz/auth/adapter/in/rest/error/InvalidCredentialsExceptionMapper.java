package de.thws.kompetenz.auth.adapter.in.rest.error;

import de.thws.kompetenz.common.exception.ErrorResponse;
import de.thws.kompetenz.user.domain.model.exception.InvalidCredentialsException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class InvalidCredentialsExceptionMapper implements ExceptionMapper<InvalidCredentialsException> {

    @Override
    public Response toResponse(InvalidCredentialsException exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage());

        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
