package de.thws.kompetenz.rating.adapter.in.rest.mapper;

import de.thws.kompetenz.rating.application.exception.SessionRatingNotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public class SessionRatingNotFoundExceptionMapper implements ExceptionMapper<SessionRatingNotFoundException> {

    @Override
    public Response toResponse(SessionRatingNotFoundException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(exception.getMessage())
                .build();
    }
}
