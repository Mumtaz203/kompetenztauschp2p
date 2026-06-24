package de.thws.kompetenz.rating.adapter.in.rest.mapper;

import de.thws.kompetenz.rating.application.exception.SessionRatingNotAuthorizedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SessionRatingNotAuthorizedExceptionMapper implements ExceptionMapper<SessionRatingNotAuthorizedException> {

    @Override
    public Response toResponse(SessionRatingNotAuthorizedException exception) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(exception.getMessage())
                .build();
    }
}