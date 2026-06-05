package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.service.scoring.HybridSkillSearchRelevanceScorer;
import de.thws.kompetenz.matching.application.service.scoring.MatchRanking;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchUserServiceHybridTest {

    @Mock
    UserRepositoryPort userRepositoryPort;

    @Mock
    SemanticSkillSearchService semanticSkillSearchService;

    @Mock
    HybridSkillSearchRelevanceScorer hybridScorer;

    SearchUserService searchUserService;

    private User sqlExactUser;
    private User mysqlUser;
    private User postgresUser;
    private User javaUser;

    @BeforeEach
    void setUp() {
        searchUserService = new SearchUserService(userRepositoryPort, semanticSkillSearchService, hybridScorer);
        sqlExactUser = user("sql_user", "sql");
        mysqlUser = user("mysql_user", "mysql");
        postgresUser = user("postgres_user", "postgresql");
        javaUser = user("java_user", "java");
    }

    @Test
    void searchBySkills_sortsResultsByScore() {
        // Setup: lexical search returns candidates
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql"))))
                .thenReturn(List.of(mysqlUser, postgresUser, sqlExactUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        
        // Setup: scoring returns: postgres(3), mysql(3), sql(10)
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    String username = u.getUsername();
                    if ("sql_user".equals(username)) {
                        return new MatchRanking(10, 1, 1, 0, 0, 1);  // exact match, highest score
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);       // partial match, lower score
                });

        // Execute
        List<User> results = searchUserService.searchBySkills(List.of("sql"));

        // Assert: expect 3 results
        assertEquals(3, results.size());
        // Most important: sql_user should be first (highest score)
        assertEquals("sql_user", results.get(0).getUsername());
        // The other two should have lower scores
        assertTrue(results.stream().skip(1).allMatch(u -> 
            "mysql_user".equals(u.getUsername()) || "postgres_user".equals(u.getUsername())
        ));
    }

    @Test
    void searchBySkills_deduplicatesUsers() {
        User duplicateUser = user("duplicate", "skill");
        when(userRepositoryPort.findCandidatesByOfferedSkills(any()))
                .thenReturn(List.of(duplicateUser, duplicateUser, javaUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> new MatchRanking(10, 1, 1, 0, 0, 1));

        List<User> results = searchUserService.searchBySkills(List.of("skill"));

        assertEquals(2, results.size());
        assertEquals(1, results.stream().filter(u -> "duplicate".equals(u.getUsername())).count());
    }

    @Test
    void searchBySkills_filtersZeroScores() {
        when(userRepositoryPort.findCandidatesByOfferedSkills(any()))
                .thenReturn(List.of(sqlExactUser, mysqlUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> new MatchRanking(0, 0, 0, 0, 0, 0));

        List<User> results = searchUserService.searchBySkills(List.of("sql"));

        assertTrue(results.isEmpty());
    }

    @Test
    void searchBySkill_delegatesToSearchBySkills() {
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("java"))))
                .thenReturn(List.of(javaUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenReturn(new MatchRanking(10, 1, 1, 0, 0, 1));

        List<User> results = searchUserService.searchBySkill("java");

        assertEquals(1, results.size());
        assertEquals("java_user", results.getFirst().getUsername());
    }

    private User user(String username, String... skills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(skills));
        return user;
    }
}
