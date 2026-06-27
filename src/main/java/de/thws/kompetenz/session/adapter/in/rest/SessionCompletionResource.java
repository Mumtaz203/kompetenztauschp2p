package de.thws.kompetenz.session.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.session.adapter.in.rest.dto.SessionCompletionResultResponse;
import de.thws.kompetenz.session.adapter.in.rest.dto.SubmitSessionCompletionResponseRequest;
import de.thws.kompetenz.session.adapter.in.rest.mapper.SessionCompletionRestMapper;
import de.thws.kompetenz.session.application.port.in.ISubmitSessionCompletionResponseUseCase;
import de.thws.kompetenz.session.domain.SkillSession;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionCompletionResource {
    @Inject
    ISubmitSessionCompletionResponseUseCase submitSessionCompletionResponseUseCase;

    @Inject
    AuthorizationGuard authorizationGuard;

    @Inject
    SessionCompletionRestMapper mapper;

    @POST
    @Path("/{sessionId}/completion-response")
    @RolesAllowed("USER")
    public Response submitCompletionResponse(
            @PathParam("sessionId") UUID sessionId,
            @Valid SubmitSessionCompletionResponseRequest request
    ) {
        UUID currentUserId = authorizationGuard.currentUserId();

        SkillSession updatedSession =
                submitSessionCompletionResponseUseCase.submitCompletionResponse(
                        sessionId,
                        currentUserId,
                        request.answer(),
                        request.reason()
                );

        SessionCompletionResultResponse response = mapper.toResponse(updatedSession);

        return Response.ok(response).build();
    }
}
