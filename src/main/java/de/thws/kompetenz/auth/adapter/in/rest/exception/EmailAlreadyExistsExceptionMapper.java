package de.thws.kompetenz.auth.adapter.in.rest.exception;

import de.thws.kompetenz.auth.adapter.in.rest.error.ErrorResponse;
import de.thws.kompetenz.auth.application.exception.EmailAlreadyExistsException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class EmailAlreadyExistsExceptionMapper implements ExceptionMapper<EmailAlreadyExistsException> {

    @Override
    public Response toResponse(EmailAlreadyExistsException exception) {
        ErrorResponse response = new ErrorResponse(exception.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }
}
