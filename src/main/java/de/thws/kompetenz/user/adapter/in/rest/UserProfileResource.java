package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.adapter.in.rest.mapper.UserRestMapper;
import de.thws.kompetenz.user.application.port.in.UpdateUserProfileUseCase;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateNameRequest;
import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateSkillsRequest;
import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateUserRequest;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserProfileResource {
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final UserRestMapper userRestMapper;

    public UserProfileResource(UpdateUserProfileUseCase updateUserProfileUseCase, UserRestMapper userRestMapper) {
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.userRestMapper = userRestMapper;
    }
    @PUT
    @Path("/{id}/updateName")
    public Response updateName(@PathParam("id") UUID userId, @Valid UpdateNameRequest updateNameRequest) {
        User updated=updateUserProfileUseCase.updateName(userId,updateNameRequest.getName());
        return Response.ok(userRestMapper.toUpdateProfileResponse(updated)).build();

    }

    @PUT
    @Path("/{id}/updateUser")
    public Response updateUser(@PathParam("id") UUID userId, @Valid UpdateUserRequest updateUserRequest) {
        User incoming = new User();
        incoming.setUsername(updateUserRequest.getUsername());
        incoming.setOfferedSkills(updateUserRequest.getOfferedSkills());
        incoming.setWantedSkills(updateUserRequest.getWantedSkills());

        User updated=updateUserProfileUseCase.updateUser(userId,incoming);
        return Response.ok(userRestMapper.toUpdateProfileResponse(updated)).build();
    }


    @PUT
    @Path("/{id}/updateSkills")
    public Response updateSkills(@PathParam("id") UUID userId, @Valid UpdateSkillsRequest updateSkillsRequest) {
        User updated= updateUserProfileUseCase.updateSkills(userId,updateSkillsRequest.getOfferedSkills(),updateSkillsRequest.getWantedSkills());
        return Response.ok(userRestMapper.toUpdateProfileResponse(updated)).build();
    }

    }

