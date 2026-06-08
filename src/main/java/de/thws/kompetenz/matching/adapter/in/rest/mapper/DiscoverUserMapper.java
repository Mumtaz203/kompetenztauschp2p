package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.DiscoverUserResponse;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DiscoverUserMapper {

    public DiscoverUserResponse toDiscoverUserResponse(UserRecommendation recommendation) {
        if (recommendation == null) {
            return null;
        }

        return new DiscoverUserResponse(
                recommendation.getUserId(),
                recommendation.getUsername(),
                recommendation.getScore(),
                recommendation.getBestSimilarity(),
                recommendation.getMatchedSkills(),
                recommendation.getMatchReason()
        );
    }
}
