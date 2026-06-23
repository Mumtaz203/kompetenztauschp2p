package de.thws.kompetenz.session.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.session.adapter.in.rest.dto.CreateSessionRequest;
import de.thws.kompetenz.session.adapter.in.rest.dto.SessionResponse;
import de.thws.kompetenz.session.adapter.in.rest.mapper.SessionRestMapper;
import de.thws.kompetenz.session.application.port.in.*;
import de.thws.kompetenz.session.domain.SkillSession;
import io.quarkus.resteasy.reactive.server.runtime.StandardSecurityCheckInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.List;
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
    @Inject
    AuthorizationGuard authorizationGuard;


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

        authorizationGuard.requireSelfOrAdmin(request.requesterUserId());
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
    @Path("/get-all-sessions")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getAllSessions(){

        List<SessionResponse> respone = getSessionUseCase.getAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(respone).build();
    }
    @GET
    @Path("/{sessionId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getSession(@PathParam("sessionId") UUID sessionId) {
        SkillSession  session= getSessionUseCase.findById(sessionId).orElseThrow(()
                -> new NotFoundException("Session not found with id: " + sessionId));

        UUID activeUserId=authorizationGuard.currentUserId();
        authorizationGuard.requireParticipantOrAdmin(session.hasParticipant(activeUserId));

        SessionResponse sessionResponse = mapper.toResponse(session);
        return  Response.ok(sessionResponse).build();
    }

    @GET
    @Path("/{sessionId}/participants/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response isParticipant(
            @PathParam("sessionId") UUID sessionId,
            @PathParam("userId") UUID userId
    ) {
        authorizationGuard.requireSelfOrAdmin(userId);

        boolean participant = getSessionUseCase.isParticipant(sessionId, userId);
        return Response.ok(participant).build();
    }

    @PATCH
    @Path("/{sessionId}/expire-rating-window-temp-testing")
    @RolesAllowed("ADMIN")
    public Response expireRatingWindowForTesting(@PathParam("sessionId") UUID sessionId) {
        try {
            authorizationGuard.requireAdmin();

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
            authorizationGuard.requireAdmin();

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
            SkillSession session= getSessionUseCase.findByMatchingRequestId(matchingRequestId)
                    .orElseThrow(() -> new NotFoundException("Session not found for matching request id: " + matchingRequestId));

            authorizationGuard.requireParticipantOrAdmin(session.hasParticipant(authorizationGuard.currentUserId()));

        return Response.ok(mapper.toResponse(session)).build();

    }
}