package de.thws.kompetenz.session.adapter.in.rest;

import de.thws.kompetenz.session.adapter.in.rest.dto.CreateSessionRequest;
import de.thws.kompetenz.session.adapter.in.rest.mapper.SessionRestMapper;
import de.thws.kompetenz.session.application.port.in.ICreateSessionUseCase;
import de.thws.kompetenz.session.application.port.in.IGetSessionUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    private final ICreateSessionUseCase createSessionUseCase;
    private final IGetSessionUseCase getSessionUseCase;
    private final SessionRestMapper mapper;

    public SessionResource(
            ICreateSessionUseCase createSessionUseCase,
            IGetSessionUseCase getSessionUseCase,
            SessionRestMapper mapper
    ) {
        this.createSessionUseCase = createSessionUseCase;
        this.getSessionUseCase = getSessionUseCase;
        this.mapper = mapper;
    }

    @POST
    @RolesAllowed({"USER", "ADMIN"})
    public Response createSession(CreateSessionRequest request) {
        var session = createSessionUseCase.createSession(
                request.requesterUserId(),
                request.receiverUserId()
        );

        return Response.status(Response.Status.CREATED)
                .entity(mapper.toResponse(session))
                .build();
    }

    @GET
    @Path("/{sessionId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getSession(@PathParam("sessionId") UUID sessionId) {
        return getSessionUseCase.findById(sessionId)
                .map(session -> Response.ok(mapper.toResponse(session)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{sessionId}/participants/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response isParticipant(
            @PathParam("sessionId") UUID sessionId,
            @PathParam("userId") UUID userId
    ) {
        boolean participant = getSessionUseCase.isParticipant(sessionId, userId);
        return Response.ok(participant).build();
    }
}