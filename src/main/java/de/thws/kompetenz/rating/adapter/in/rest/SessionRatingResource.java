package de.thws.kompetenz.rating.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.rating.adapter.in.rest.dto.CreateSessionRatingRequest;
import de.thws.kompetenz.rating.adapter.in.rest.dto.RatingSummaryResponce;
import de.thws.kompetenz.rating.adapter.in.rest.dto.SessionRatingResponse;
import de.thws.kompetenz.rating.adapter.in.rest.dto.UpdateRatingStatusRequest;
import de.thws.kompetenz.rating.adapter.in.rest.mapper.SessionRatingMapper;
import de.thws.kompetenz.rating.application.in.*;
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

    @Inject
    IGetUserRatingUseCase getUserRatingUseCase;

    @Inject
    AuthorizationGuard authorizationGuard;

    @Inject
    IUpdateSessionRatingStatusUseCase updateRatingStatusUseCase;

    @POST
    @Path("/create/")
    @RolesAllowed("USER")
    public Response createSessionRating(@Valid CreateSessionRatingRequest request){
        UUID senderUserId = authorizationGuard.currentUserId();


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
    }

    @POST
    @Path("/sessions/{sessionId}/publish")
    @RolesAllowed("ADMIN")
    public Response publishRatingsForSession(@PathParam("sessionId") UUID sessionId) {

            List<SessionRatingResponse> response = publishSessionRatingsUseCase.publishRatingsForSession(sessionId)
                    .stream()
                    .map(mapper::toResponse)
                    .toList();

            return Response.ok(response).build();
    }

    @GET
    @Path("/users/{userId}/summary")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getRatingSummaryForUser(@PathParam("userId") UUID userId) {

    authorizationGuard.requireSelfOrAdmin(userId);

    RatingSummaryResponce response = mapper.toSummaryResponse(
            getRatingSummaryUseCase.getRatingSummaryForUser(userId)
    );

    return Response.ok(response).build();
    }

    @GET
    @Path("/users/{userId}")
    @RolesAllowed("ADMIN")
    public Response getPublishedRatingsForUser(@PathParam("userId") UUID userId) {

            List<SessionRatingResponse> response = getUserRatingUseCase.getPublishedRatingsForUser(userId)
                    .stream()
                    .map(mapper::toResponse)
                    .toList();

            return Response.ok(response).build();

    }

    @GET
    @Path("/me")
    @RolesAllowed("USER")
    public Response getMyRatings() {
        UUID currentUserId = authorizationGuard.currentUserId();

        List<SessionRatingResponse> ratings = getUserRatingUseCase.getOwnRatings(currentUserId)
                .stream()
                .map(r -> mapper.toResponse(r))
                .toList();

        return Response.ok(ratings).build();

    }

    @GET
    @Path("/{ratingId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getVisibleRatingById(@PathParam("ratingId") UUID ratingId){
        SessionRating rating = getUserRatingUseCase.getVisibleRating(ratingId, authorizationGuard.currentUserId(),
                authorizationGuard.isAdmin());

        return Response.ok(mapper.toResponse(rating)).build();

    }

    @GET
    @Path("/get-all-ratings")
    @RolesAllowed("ADMIN")
    public Response getAllRatings(){
        List<SessionRatingResponse> responses = getUserRatingUseCase.getAllRatings()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(responses).build();
    }

    @GET
    @Path("/admin/published-ratings")
    @RolesAllowed("ADMIN")
    public Response getAllPublishedRatings(){

        List<SessionRatingResponse> responses = getUserRatingUseCase.getAllPublishedRatings()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(responses).build();
    }
    @GET
    @Path("/admin/non-published")
    @RolesAllowed("ADMIN")
    public Response getAllNonPublishedRatings() {
        List<SessionRatingResponse> response = getUserRatingUseCase.getAllNonPublishedRatings()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(response).build();
    }

    @GET
    @Path("/admin/users/{userId}")
    @RolesAllowed("ADMIN")
    public Response getAllRatingsForUser(@PathParam("userId") UUID userId) {
        List<SessionRatingResponse> response = getUserRatingUseCase.getAllRatingsForUser(userId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(response).build();
    }

    @GET
    @Path("/admin/{ratingId}")
    @RolesAllowed("ADMIN")
    public Response getRatingByIdForAdmin(@PathParam("ratingId") UUID ratingId) {
        SessionRating rating = getUserRatingUseCase.getRating(ratingId);

        return Response.ok(mapper.toResponse(rating)).build();
    }

    @GET
    @Path("/admin/published/{ratingId}")
    @RolesAllowed("ADMIN")
    public Response getPublishedRatingByIdForAdmin(@PathParam("ratingId") UUID ratingId) {
        SessionRating rating = getUserRatingUseCase.getPublishedRating(ratingId);

        return Response.ok(mapper.toResponse(rating)).build();
    }

    @GET
    @Path("/admin/non-published/{ratingId}")
    @RolesAllowed("ADMIN")
    public Response getNonPublishedRatingByIdForAdmin(@PathParam("ratingId") UUID ratingId) {
        SessionRating rating = getUserRatingUseCase.getNonPublishedRating(ratingId);

        return Response.ok(mapper.toResponse(rating)).build();
    }

    @PATCH
    @Path("/admin/{ratingId}/status")
    @RolesAllowed("ADMIN")
    public Response updateRatingStatus(
            @PathParam("ratingId") UUID ratingId,
            @Valid UpdateRatingStatusRequest request
    ) {
        SessionRating updatedRating = updateRatingStatusUseCase.updateRatingStatus(
                ratingId,
                request.status()
        );

        return Response.ok(mapper.toResponse(updatedRating)).build();
    }
}