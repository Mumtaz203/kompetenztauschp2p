package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetAllUsersResponse;
import de.thws.kompetenz.user.adapter.in.rest.mapper.UserRestMapper;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserResource {
    private final UserRepositoryPort userRepositoryPort;
    private final UserRestMapper userRestMapper;

    public UserResource(UserRepositoryPort userRepositoryPort, UserRestMapper userRestMapper) {
        this.userRepositoryPort = userRepositoryPort;
        this.userRestMapper = userRestMapper;
    }

    @GET
    @Path("/getAllUsers")
    public Response getAllUsers() {
        GetAllUsersResponse response = userRestMapper.toGetAllUsersResponse(userRepositoryPort.findAllUsers());
        return Response.ok(response).build();
    }

    @GET
    @Path("/getUser/{id}")
    public Response getUserById(@PathParam("id") UUID id) {
        return userRepositoryPort.findUserById(id)
                .map(userRestMapper::toGetUserResponse)
                .map(user -> Response.ok(user).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
}
