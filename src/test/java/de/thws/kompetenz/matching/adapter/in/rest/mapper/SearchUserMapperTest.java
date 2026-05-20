package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchUserMapperTest {

    private final SearchUserMapper mapper = new SearchUserMapper();

    @Test
    void toSearchUserResponse_returnsEmptyListsForNullSkills() {
        User user = new User(UUID.randomUUID(), "tester", "tester@test.com", "secret");

        SearchUserResponse response = mapper.toSearchUserResponse(user);

        assertNotNull(response);
        assertEquals(List.of(), response.getOfferedSkills());
        assertEquals(List.of(), response.getWantedSkills());
    }

    @Test
    void toSearchUserResponse_deduplicatesSkillsAndPreservesOrder() {
        User user = new User(UUID.randomUUID(), "tester", "tester@test.com", "secret");
        user.setOfferedSkills(new ArrayList<>(List.of("sql", "sql", " java ")));
        user.setWantedSkills(List.of("spring", "spring"));

        SearchUserResponse response = mapper.toSearchUserResponse(user);

        assertEquals(List.of("sql", "java"), response.getOfferedSkills());
        assertEquals(List.of("spring"), response.getWantedSkills());
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
