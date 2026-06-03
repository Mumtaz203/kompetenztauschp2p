package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetAllUsersResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.UserResponse;
import de.thws.kompetenz.user.adapter.in.rest.mapper.UserRestMapper;
import de.thws.kompetenz.user.application.port.in.UserUseCaseI;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.annotation.security.RolesAllowed;
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
    private final UserUseCaseI userUseCase;

    public UserResource(UserRepositoryPort userRepositoryPort, UserRestMapper userRestMapper, UserUseCaseI userUseCase) {
        this.userRepositoryPort = userRepositoryPort;
        this.userRestMapper = userRestMapper;
        this.userUseCase = userUseCase;
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
    @DELETE
    @Path("/deleteUser/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteUserById(@PathParam("id") UUID id) {
        User user = userUseCase.deleteUserById(id);
        UserResponse userResponse = userRestMapper.toUserResponse(user);
        return Response.ok(userResponse).build();
    }

    @PUT
    @Path("/updateUser/{id}")
    @RolesAllowed("ADMIN")
    public Response updateUserById(@PathParam("id") UUID id, User user) {
        User updatedUser=userUseCase.updateUser(id, user);
        UserResponse userResponse = userRestMapper.toUserResponse(updatedUser);
        return Response.ok(userResponse).build();
    }
}
