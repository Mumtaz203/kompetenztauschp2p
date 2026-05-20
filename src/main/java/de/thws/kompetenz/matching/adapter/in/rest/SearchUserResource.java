package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.matching.adapter.in.rest.mapper.SearchUserMapper;
import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
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
    public Response searchUserBySkill(
            @QueryParam("skill") String skill,
            @QueryParam("skills") String skills) {
        List<String> searchTerms = SkillSearchTermsParser.parse(skill, skills);

        String validationError = SkillSearchTermsParser.validate(searchTerms);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(validationError)
                    .build();
        }

        List<SearchUserResponse> response = new ArrayList<>();
        for (var user : searchUserUseCase.searchBySkills(searchTerms)) {
            SearchUserResponse mapped = searchUserMapper.toSearchUserResponse(user);
            if (mapped != null) {
                response.add(mapped);
            }
        }

        return Response.ok(response).build();
    }
}
