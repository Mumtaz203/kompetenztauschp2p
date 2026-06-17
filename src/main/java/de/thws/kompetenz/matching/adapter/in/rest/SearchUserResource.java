package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.matching.adapter.in.rest.mapper.SearchUserMapper;
import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.domain.RatingSummary;
import jakarta.annotation.security.RolesAllowed;
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
    private final IGetRatingSummaryUseCase getRatingSummaryUseCase;

    public SearchUserResource(SearchUserUseCase searchUserUseCase, SearchUserMapper searchUserMapper,
                              IGetRatingSummaryUseCase getRatingSummaryUseCase) {
        this.searchUserUseCase = searchUserUseCase;
        this.searchUserMapper = searchUserMapper;
        this.getRatingSummaryUseCase = getRatingSummaryUseCase;
    }

    @GET
    @Path("/search")
    @RolesAllowed("USER")
    public Response searchUserBySkill(//no need to implement a guard check i think its a basik method without userId in path and its only for searching users by skill so we can let all users access it
            @QueryParam("skill") String skill,
            @QueryParam("skills") String skills
    ) {
        List<String> searchTerms = SkillSearchTermsParser.parse(skill, skills);

        String validationError = SkillSearchTermsParser.validate(searchTerms);
        if (validationError != null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(validationError)
                    .build();
        }

        List<SearchUserResponse> response = new ArrayList<>();

        for (var user : searchUserUseCase.searchBySkills(searchTerms)) {
            RatingSummary ratingSummary = getRatingSummaryUseCase.getRatingSummaryForUser(user.getId());

            SearchUserResponse mapped = searchUserMapper.toSearchUserResponse(user, ratingSummary);

            if (mapped != null) {
                response.add(mapped);
            }
        }

        return Response.ok(response).build();
    }
}
