package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.service.scoring.MatchRanking;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchUserServiceComparatorTest {

    @Test
    void testComparatorSortsCorrectly() {
        // Create a comparator like SearchUserService uses
        Comparator<RankedUser> byRelevance = Comparator
                .comparingInt(RankedUser::score)
                .thenComparingInt(RankedUser::matchedTermCount)
                .reversed();

        User user1 = new User(UUID.randomUUID(), "user1", "user1@test.com", "secret");
        User user2 = new User(UUID.randomUUID(), "user2", "user2@test.com", "secret");
        User user3 = new User(UUID.randomUUID(), "user3", "user3@test.com", "secret");

        RankedUser ranked1 = new RankedUser(user1, 10, 1, 0, 0, 0, 0);
        RankedUser ranked2 = new RankedUser(user2, 3, 1, 0, 0, 0, 0);
        RankedUser ranked3 = new RankedUser(user3, 3, 1, 0, 0, 0, 0);

        List<RankedUser> results = new ArrayList<>();
        results.add(ranked2);
        results.add(ranked3);
        results.add(ranked1);

        results.sort(byRelevance);

        assertEquals(ranked1.user().getUsername(), results.get(0).user().getUsername(), "Highest score (10) should come first");
        assertEquals(ranked2.user().getUsername(), results.get(1).user().getUsername());
        assertEquals(ranked3.user().getUsername(), results.get(2).user().getUsername());
    }

    private record RankedUser(
            User user,
            int score,
            int matchedTermCount,
            int exactMatchCount,
            int semanticMatchCount,
            int partialMatchCount,
            int offeredSkillsCount
    ) {
    }
}
