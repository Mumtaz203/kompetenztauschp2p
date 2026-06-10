package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SearchUserMapperTest {

    private final SearchUserMapper mapper = new SearchUserMapper();

    @Test
    void toSearchUserResponse_returnsEmptyListsForNullSkills() {
        User user = new User(UUID.randomUUID(), "tester", "tester@test.com", "secret");
        RatingSummary ratingSummary = new RatingSummary(BigDecimal.ZERO, 0);

        SearchUserResponse response = mapper.toSearchUserResponse(user, ratingSummary);

        assertNotNull(response);
        assertEquals(List.of(), response.getOfferedSkills());
        assertEquals(List.of(), response.getWantedSkills());
        assertEquals(BigDecimal.ZERO, response.getAveragePoints());
        assertEquals(0, response.getRatingCount());
    }

    @Test
    void toSearchUserResponse_deduplicatesSkillsAndPreservesOrder() {
        User user = new User(UUID.randomUUID(), "tester", "tester@test.com", "secret");
        user.setOfferedSkills(new ArrayList<>(List.of("sql", "sql", " java ")));
        user.setWantedSkills(List.of("spring", "spring"));

        RatingSummary ratingSummary = new RatingSummary(BigDecimal.valueOf(4.5), 2);

        SearchUserResponse response = mapper.toSearchUserResponse(user, ratingSummary);

        assertEquals(List.of("sql", "java"), response.getOfferedSkills());
        assertEquals(List.of("spring"), response.getWantedSkills());
        assertEquals(BigDecimal.valueOf(4.5), response.getAveragePoints());
        assertEquals(2, response.getRatingCount());
    }

    @Test
    void toSearchUserResponse_usesZeroRatingSummaryWhenNull() {
        User user = new User(UUID.randomUUID(), "tester", "tester@test.com", "secret");

        SearchUserResponse response = mapper.toSearchUserResponse(user, null);

        assertNotNull(response);
        assertEquals(BigDecimal.ZERO, response.getAveragePoints());
        assertEquals(0, response.getRatingCount());
    }

    @Test
    void gettersReturnEmptyListsWhenFieldsAreNull() {
        SearchUserResponse response = new SearchUserResponse();
        response.setOfferedSkills(null);
        response.setWantedSkills(null);

        assertEquals(List.of(), response.getOfferedSkills());
        assertEquals(List.of(), response.getWantedSkills());
    }
}