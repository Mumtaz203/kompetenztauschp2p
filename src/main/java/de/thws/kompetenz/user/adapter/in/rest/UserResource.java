package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetAllUsersResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.UpdateInternalFlagRequest;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.UserResponse;
import de.thws.kompetenz.user.adapter.in.rest.mapper.UserRestMapper;
import de.thws.kompetenz.user.application.port.in.UserUseCaseI;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.security.spi.runtime.AuthorizationController;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserResource {
    private final UserRepositoryPort userRepositoryPort;
    private final UserRestMapper userRestMapper;
    private final UserUseCaseI userUseCase;
    @Inject
    AuthorizationController authorizationController;
    private IGetRatingSummaryUseCase getRatingSummaryUseCase;
    @Inject
    private AuthorizationGuard authenticationGuard;

    public UserResource(UserRepositoryPort userRepositoryPort, UserRestMapper userRestMapper, UserUseCaseI userUseCase,
                        IGetRatingSummaryUseCase getRatingSummaryUseCase) {
        this.userRepositoryPort = userRepositoryPort;
        this.userRestMapper = userRestMapper;
        this.userUseCase = userUseCase;
        this.getRatingSummaryUseCase = getRatingSummaryUseCase;
    }

    @GET
    @Path("/getAllUsers")
    @RolesAllowed("ADMIN")
    public Response getAllUsers() {

        authenticationGuard.requireAdmin();


        List<User> users = userRepositoryPort.findAllUsers();

        Map<UUID, RatingSummary> ratingSummaries = new HashMap<>();

        for (User user : users) {
            RatingSummary summary = getRatingSummaryUseCase.getRatingSummaryForUser(user.getId());
            ratingSummaries.put(user.getId(), summary);
        }

        GetAllUsersResponse response = userRestMapper.toGetAllUsersResponse(users, ratingSummaries);

        return Response.ok(response).build();
    }

    @GET
    @Path("/getRandom10Users")
    @RolesAllowed("USER")
    public Response getRandom10Users(){

        List<User> users = userRepositoryPort.findRandom10Users();

        Map<UUID, RatingSummary> ratingSummaries = new HashMap<>();
        for (User user : users) {
            RatingSummary summary = getRatingSummaryUseCase.getRatingSummaryForUser(user.getId());
            ratingSummaries.put(user.getId(), summary);
        }

        GetAllUsersResponse response = userRestMapper.toGetAllUsersResponse(users, ratingSummaries);
        return Response.ok(response).build();
    }

    @GET
    @Path("/getUser/{id}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getUserById(@PathParam("id") UUID id) {


        authenticationGuard.requireSelfOrAdmin(id);

        return userRepositoryPort.findUserById(id)
                .map(user -> {
                    RatingSummary ratingSummary1 = getRatingSummaryUseCase.getRatingSummaryForUser(user.getId());
                    return userRestMapper.toGetUserResponse(user, ratingSummary1);
                })
                .map(userReponse -> Response.ok(userReponse).build())
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
    }
    @DELETE
    @Path("/deleteUser/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteUserById(@PathParam("id") UUID id) {
        authenticationGuard.requireAdmin();
        User user = userUseCase.deleteUserById(id);
        UserResponse userResponse = userRestMapper.toUserResponse(user);
        return Response.ok(userResponse).build();
    }

    @PUT
    @Path("/updateUser/{id}")
    @RolesAllowed("ADMIN")//no need to implement with guard its sowieso just for admin
    //I JUST CHANGED MY MIND AND ADDED ADMIN FORCE
    public Response updateUserById(@PathParam("id") UUID id, User user) {
        authenticationGuard.requireAdmin();
        User updatedUser=userUseCase.updateUser(id, user);
        UserResponse userResponse = userRestMapper.toUserResponse(updatedUser);
        return Response.ok(userResponse).build();
        
    }

    @PATCH
    @Path("/admin/{id}/internal-flag")
    @RolesAllowed("ADMIN")
    public Response updateInternalFlag(
            @PathParam("id") UUID id,
            @Valid UpdateInternalFlagRequest request
    ) {
        authenticationGuard.requireAdmin();

        User updatedUser = userUseCase.updateInternalFlag(id, request.internallyFlagged());

        return Response.ok(userRestMapper.toGetUserResponse(
                updatedUser,
                getRatingSummaryUseCase.getRatingSummaryForUser(updatedUser.getId())
        )).build();
    }
}
