package de.thws.kompetenz.rating.adapter.in.rest;

import de.thws.kompetenz.rating.adapter.in.rest.dto.CreateSessionRatingRequest;
import de.thws.kompetenz.rating.adapter.in.rest.dto.RatingSummaryResponce;
import de.thws.kompetenz.rating.adapter.in.rest.dto.SessionRatingResponse;
import de.thws.kompetenz.rating.adapter.in.rest.mapper.SessionRatingMapper;
import de.thws.kompetenz.rating.application.in.ICreateSessionRatingUseCase;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.rating.domain.SessionRating;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/ratings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class SessionRatingResource {

    @Inject
    ICreateSessionRatingUseCase createSessionRatingUseCase;
    @Inject
    SessionRatingMapper mapper;

    @Inject
    IPublishSessionRatingsUseCase publishSessionRatingsUseCase;

    @Inject
    IGetRatingSummaryUseCase getRatingSummaryUseCase;

    @POST
    @Path("/sender/{senderUserId}")
    public Response createSessionRating(@Valid CreateSessionRatingRequest request,
                                        @PathParam("senderUserId") UUID senderUserId){

        try{

            SessionRating createdRating = createSessionRatingUseCase.createRating(
                    request.sessionId(),
                    senderUserId,
                    request.receiverUserId(),
                    request.points(),
                    request.comment()
            );

            SessionRatingResponse response = mapper.toResponse(createdRating);

            return Response.status(Response.Status.CREATED)
                    .entity(response)
                    .build();

        }catch (IllegalArgumentException | IllegalStateException e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/sessions/{sessionId}/publish")
    @RolesAllowed("ADMIN")
    public Response publishRatingsForSession(@PathParam("sessionId") UUID sessionId) {
        try {

            List<SessionRatingResponse> response = publishSessionRatingsUseCase.publishRatingsForSession(sessionId)
                    .stream()
                    .map(mapper::toResponse)
                    .toList();

            return Response.ok(response).build();

        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/users/{userId}/summary")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getRatingSummaryForUser(@PathParam("userId") UUID userId) {
        try {
            RatingSummaryResponce response = mapper.toSummaryResponse(
                    getRatingSummaryUseCase.getRatingSummaryForUser(userId)
            );

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

}
