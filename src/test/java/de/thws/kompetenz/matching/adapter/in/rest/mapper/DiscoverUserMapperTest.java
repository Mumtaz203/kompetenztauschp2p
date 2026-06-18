package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.DiscoverUserResponse;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DiscoverUserMapperTest {

    private final DiscoverUserMapper mapper = new DiscoverUserMapper();

    @Test
    void toDiscoverUserResponse_mapsUserRecommendation() {
        UUID userId = UUID.randomUUID();
        UserRecommendation recommendation = new UserRecommendation(
                userId,
                "backend_user",
                "https://example.com/profile.png",
                "THWS",
                87,
                0.91,
                List.of("Java", "Spring Boot"),
                "Your wanted skills match this user's offered skills."
        );

        DiscoverUserResponse response = mapper.toDiscoverUserResponse(recommendation);

        assertEquals(userId, response.getUserId());
        assertEquals("backend_user", response.getUsername());
        assertEquals("https://example.com/profile.png", response.getProfileImageUrl());
        assertEquals("THWS", response.getUniversity());
        assertEquals(87, response.getScore());
        assertEquals(0.91, response.getBestSimilarity(), 0.0001);
        assertEquals(List.of("Java", "Spring Boot"), response.getMatchedSkills());
        assertEquals("Your wanted skills match this user's offered skills.", response.getMatchReason());
    }

    @Test
    void toDiscoverUserResponse_returnsNull_whenRecommendationIsNull() {
        assertNull(mapper.toDiscoverUserResponse(null));
    }

    @Test
    void discoverUserResponse_returnsEmptyMatchedSkills_whenFieldIsNull() {
        DiscoverUserResponse response = new DiscoverUserResponse();
        response.setMatchedSkills(null);

        assertEquals(List.of(), response.getMatchedSkills());
    }
}
