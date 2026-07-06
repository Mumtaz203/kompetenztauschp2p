package de.thws.kompetenz.session.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.session.adapter.in.rest.dto.CreatePrivateSessionReportRequest;
import de.thws.kompetenz.session.adapter.in.rest.mapper.PrivateSessionReportRestMapper;
import de.thws.kompetenz.session.application.port.in.ICreatePrivateSessionReportUseCase;
import de.thws.kompetenz.session.application.port.in.IGetPrivateSessionReportsUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PrivateSessionReportResource {

    @Inject
    ICreatePrivateSessionReportUseCase createPrivateSessionReportUseCase;

    @Inject
    IGetPrivateSessionReportsUseCase getPrivateSessionReportsUseCase;

    @Inject
    PrivateSessionReportRestMapper mapper;

    @Inject
    AuthorizationGuard authorizationGuard;

    @POST
    @Path("/{sessionId}/private-reports")
    @RolesAllowed("USER")
    public Response createReport(
            @PathParam("sessionId") UUID sessionId,
            @Valid CreatePrivateSessionReportRequest request
    ) {
        var report = createPrivateSessionReportUseCase.createReport(
                sessionId,
                authorizationGuard.currentUserId(),
                request.reportedUserId(),
                request.reasonCode(),
                request.description()
        );

        return Response.status(Response.Status.CREATED)
                .entity(mapper.toResponse(report))
                .build();
    }

    @GET
    @Path("/{sessionId}/private-reports/me/reported-users/{reportedUserId}")
    @RolesAllowed("USER")
    public Response hasCurrentUserReported(
            @PathParam("sessionId") UUID sessionId,
            @PathParam("reportedUserId") UUID reportedUserId
    ) {
        boolean reported = getPrivateSessionReportsUseCase.hasReportFromUser(
                sessionId,
                authorizationGuard.currentUserId(),
                reportedUserId
        );

        return Response.ok(Map.of("reported", reported)).build();
    }

    @GET
    @Path("/admin/private-reports")
    @RolesAllowed("ADMIN")
    public Response getAllReports() {
        authorizationGuard.requireAdmin();

        var response = getPrivateSessionReportsUseCase.getAllReports()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(response).build();
    }

    @GET
    @Path("/admin/{sessionId}/private-reports")
    @RolesAllowed("ADMIN")
    public Response getReportsForSession(@PathParam("sessionId") UUID sessionId) {
        authorizationGuard.requireAdmin();

        var response = getPrivateSessionReportsUseCase.getReportsForSession(sessionId)
                .stream()
                .map(mapper::toResponse)
                .toList();

        return Response.ok(response).build();
    }
}
