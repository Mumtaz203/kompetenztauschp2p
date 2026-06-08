package de.thws.kompetenz.session.adapter.in.rest;

import de.thws.kompetenz.session.adapter.in.rest.dto.CreateSessionRequest;
import de.thws.kompetenz.session.adapter.in.rest.mapper.SessionRestMapper;
import de.thws.kompetenz.session.application.port.in.*;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.UUID;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {

    private final ICreateSessionUseCase createSessionUseCase;
    private final IGetSessionUseCase getSessionUseCase;
    private final SessionRestMapper mapper;
    private final IOpenRatingWindowUseCase openRatingWindowUseCase;
    private final ExpireRatingWindowForTestingUseCase expireRatingWindowForTesting;

    public SessionResource(
            ICreateSessionUseCase createSessionUseCase,
            IGetSessionUseCase getSessionUseCase,
            SessionRestMapper mapper,
            IOpenRatingWindowUseCase openRatingWindowUseCase,
            ExpireRatingWindowForTestingUseCase expireRatingWindowForTesting
    ) {
        this.createSessionUseCase = createSessionUseCase;
        this.getSessionUseCase = getSessionUseCase;
        this.mapper = mapper;
        this.openRatingWindowUseCase = openRatingWindowUseCase;
        this.expireRatingWindowForTesting = expireRatingWindowForTesting;
    }

    @POST
    @RolesAllowed({"USER", "ADMIN"}) //since the session is now created at matching_request maybe make this endpoint admin only?
    public Response createSession(CreateSessionRequest request) {
        var session = createSessionUseCase.createSession(
                request.matchingRequestId(),
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

    @PATCH
    @Path("/{sessionId}/expire-rating-window-temp-testing")
    @RolesAllowed("ADMIN")
    public Response expireRatingWindowForTesting(@PathParam("sessionId") UUID sessionId) {
        try {
            var session = expireRatingWindowForTesting.expireRatingWindowForTesting(sessionId);

            return Response.ok(mapper.toResponse(session)).build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PATCH
    @Path("/{sessionId}/open-rating-window")
    @RolesAllowed("ADMIN")
    public Response openRatingWindow(@PathParam("sessionId") UUID sessionId) {
        try {
            var session = openRatingWindowUseCase.openRatingWindow(
                    sessionId,
                    LocalDateTime.now().plusDays(3)
            );

            return Response.ok(mapper.toResponse(session)).build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/by-match-request/{matchingRequestId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getSessionByMatchingRequestId(@PathParam("matchingRequestId") UUID matchingRequestId) {
        return getSessionUseCase.findByMatchingRequestId(matchingRequestId)
                .map(session -> Response.ok(mapper.toResponse(session)).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}