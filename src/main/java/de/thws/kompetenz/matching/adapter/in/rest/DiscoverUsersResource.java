package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.adapter.in.rest.dto.DiscoverUserResponse;
import de.thws.kompetenz.matching.adapter.in.rest.mapper.DiscoverUserMapper;
import de.thws.kompetenz.matching.application.service.DiscoverUsersService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class DiscoverUsersResource {

    private final DiscoverUsersService discoverUsersService;
    private final DiscoverUserMapper discoverUserMapper;

    public DiscoverUsersResource(
            DiscoverUsersService discoverUsersService,
            DiscoverUserMapper discoverUserMapper
    ) {
        this.discoverUsersService = discoverUsersService;
        this.discoverUserMapper = discoverUserMapper;
    }

    @GET
    @Path("/{userId}/discover")
    @RolesAllowed("USER")
    public Response discoverUsers(@PathParam("userId") UUID userId) {
        List<DiscoverUserResponse> response = new ArrayList<>();
        var recommendations = discoverUsersService.recommendUsers(userId);
        if (recommendations == null) {
            return Response.ok(response).build();
        }

        for (var recommendation : recommendations) {
            DiscoverUserResponse mapped = discoverUserMapper.toDiscoverUserResponse(recommendation);
            if (mapped != null) {
                response.add(mapped);
            }
        }

        return Response.ok(response).build();
    }
}
