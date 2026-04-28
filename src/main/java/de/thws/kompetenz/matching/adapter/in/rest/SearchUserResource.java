package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.matching.adapter.in.rest.mapper.SearchUserMapper;
import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;

import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/users")

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class SearchUserResource {
    private final SearchUserUseCase searchUserUseCase;
    private final SearchUserMapper searchUserMapper;
    public SearchUserResource(SearchUserUseCase searchUserUseCase, SearchUserMapper searchUserMapper) {
        this.searchUserUseCase = searchUserUseCase;
        this.searchUserMapper = searchUserMapper;
    }
    @GET
    @Path("/search")
    public Response searchUserBySkill(@QueryParam("skill") String skill) {
        if (skill == null || skill.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("skill cannot be empty")
                    .build();
        }

        String normalizedSkill = skill.trim();
        if (normalizedSkill.length() < 3) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("skill must be at least 3 characters")
                    .build();
        }

        List<SearchUserResponse> response = searchUserUseCase.searchBySkill(normalizedSkill).stream()
                .map(searchUserMapper::toSearchUserResponse)
                .toList();

        return Response.ok(response).build();
    }

}
