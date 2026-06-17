package de.thws.kompetenz.matching_request.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.matching_request.adapter.in.rest.dto.MatchRequestResponseDTO;

import de.thws.kompetenz.matching_request.adapter.in.rest.mapper.MatchRequestRestMapper;
import de.thws.kompetenz.matching_request.application.port.in.MatchRequestUseCaseI;
import de.thws.kompetenz.matching_request.domain.MatchRequestModel;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/match-requests")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class MatchRequestResource {

    @Inject
    private MatchRequestUseCaseI useCase;

    @Inject
    private MatchRequestRestMapper mapper;

    @Inject
    private AuthorizationGuard authorizationGuard;

    @POST
    @Path("/send/senderId/{senderId}/receiverId/{receiverId}")
    @RolesAllowed("USER")
    public Response sendMatchRequest(@PathParam("senderId") UUID senderId, @PathParam("receiverId")UUID receiverId) {
        try {
            authorizationGuard.requireSelfOrAdmin(senderId);

            MatchRequestModel model = useCase.sendRequest(senderId, receiverId);
            MatchRequestResponseDTO response = mapper.toResponseDTO(model);
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/incoming/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getIncomingRequests(@PathParam("userId") UUID userId) {
        try {
            authorizationGuard.requireSelfOrAdmin(userId);

            List<MatchRequestModel> models = useCase.getIncomingPendingRequests(userId);
            List<MatchRequestResponseDTO> response = mapper.toResponseDTOList(models);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/outgoing/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getOutgoingRequests(@PathParam("userId") UUID userId) {

        try {
            authorizationGuard.requireSelfOrAdmin(userId);

            List<MatchRequestModel> models = useCase.getOutgoingPendingRequests(userId);
            List<MatchRequestResponseDTO> response = mapper.toResponseDTOList(models);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/matches/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getMatches(@PathParam("userId") UUID userId) {
        try {
            authorizationGuard.requireSelfOrAdmin(userId);

            List<MatchRequestModel> models = useCase.getAcceptedMatches(userId);
            List<MatchRequestResponseDTO> response = mapper.toResponseDTOList(models);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/{requestId}/accept/{actingUserId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response acceptRequest(@PathParam("requestId") UUID requestId,
                                  @PathParam("actingUserId") UUID actingUserId) {
        try {
            authorizationGuard.requireSelfOrAdmin(actingUserId);

            MatchRequestModel model = useCase.acceptRequest(requestId, actingUserId);
            MatchRequestResponseDTO response = mapper.toResponseDTO(model);
            return Response.ok(response).build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/{requestId}/reject/{actingUserId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response rejectRequest(@PathParam("requestId") UUID requestId,
                                  @PathParam("actingUserId") UUID actingUserId) {
        try {
            authorizationGuard.requireSelfOrAdmin(actingUserId);

            useCase.rejectRequest(requestId, actingUserId);
            return Response.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @PATCH
    @Path("/updateMatchRequest/{requestId}")
    @RolesAllowed("ADMIN")
    public Response adminUpdateRequest(@PathParam("requestId") UUID requestId, MatchRequestResponseDTO dto) {
        try {
            authorizationGuard.requireAdmin();


            MatchRequestModel model = new MatchRequestModel();
            model.setId(requestId);
            model.setStatus(dto.getStatus());
            model.setSenderId(dto.getSenderId());
            model.setReceiverId(dto.getReceiverId());

            MatchRequestModel updated = useCase.adminUpdate(requestId, model);
            MatchRequestResponseDTO response = mapper.toResponseDTO(updated);
            return Response.ok(response).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/deleteMatchRequest/{requestId}")
    @RolesAllowed("ADMIN")
    public Response adminDeleteRequest(@PathParam("requestId") UUID requestId) {
        try {
            authorizationGuard.requireAdmin();

            useCase.adminDelete(requestId);
            return Response.noContent().build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}