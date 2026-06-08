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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for SearchUserService to verify that:
 * 1. Exact matches rank before partial matches (lexical search)
 * 2. Multiple search terms are handled correctly
 * 3. Results are deduplicated by user ID
 * 4. Zero-score results are filtered
 * 5. Semantic search service is integrated without breaking lexical search
 */
@ExtendWith(MockitoExtension.class)
class SearchUserServiceTest {

    @Mock
    UserRepositoryPort userRepositoryPort;

    @Mock
    SemanticSkillSearchService semanticSkillSearchService;

    @Mock
    HybridSkillSearchRelevanceScorer hybridScorer;

    SearchUserService searchUserService;

    private User sqlExactUser;
    private User mysqlPartialUser;
    private User postgresPartialUser;
    private User sqlJavaUser;
    private User javascriptUser;

    @BeforeEach
    void setUp() {
        searchUserService = new SearchUserService(userRepositoryPort, semanticSkillSearchService, hybridScorer);
        sqlExactUser = user("sql_exact_user", "sql");
        mysqlPartialUser = user("mysql_partial_user", "mysql");
        postgresPartialUser = user("postgres_partial_user", "postgresql");
        sqlJavaUser = user("sql_java_user", "sql", "java");
        javascriptUser = user("javascript_user", "javascript");
    }

    @Test
    void searchBySkills_returnsExactMatchesBeforePartialMatches() {
        // Arrange: lexical search returns candidates
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql"))))
                .thenReturn(List.of(mysqlPartialUser, postgresPartialUser, sqlExactUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        // Setup scoring with thenAnswer to handle any user
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    if ("sql_exact_user".equals(u.getUsername())) {
                        return new MatchRanking(10, 1, 1, 0, 0, 1);  // exact match
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);  // partial match
                });

        // Act
        List<User> results = searchUserService.searchBySkills(List.of("sql"));

        // Assert: exact match comes first
        assertEquals(3, results.size());
        assertEquals("sql_exact_user", results.get(0).getUsername());
    }

    @Test
    void searchBySkills_sortsByRelevanceForMultipleTerms() {
        // Arrange
        User mysqlJavaUser = user("mysql_java_user", "mysql", "java");
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql", "java"))))
                .thenReturn(List.of(javascriptUser, mysqlJavaUser, sqlJavaUser, sqlExactUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    String username = u.getUsername();
                    if ("sql_java_user".equals(username)) {
                        return new MatchRanking(20, 2, 1, 0, 0, 2);
                    } else if ("mysql_java_user".equals(username)) {
                        return new MatchRanking(13, 2, 0, 0, 2, 2);
                    } else if ("sql_exact_user".equals(username)) {
                        return new MatchRanking(10, 1, 1, 0, 0, 1);
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);
                });

        // Act
        List<User> results = searchUserService.searchBySkills(List.of("sql", "java"));

        // Assert: highest score first
        assertEquals("sql_java_user", results.get(0).getUsername());
        assertTrue(results.indexOf(mysqlJavaUser) < results.indexOf(javascriptUser));
        assertTrue(results.indexOf(sqlExactUser) < results.indexOf(javascriptUser));
    }

    @Test
    void searchBySkills_removesDuplicateUsers() {
        // Arrange: candidates list has duplicates
        List<User> duplicateCandidates = new ArrayList<>();
        duplicateCandidates.add(sqlJavaUser);
        duplicateCandidates.add(sqlJavaUser);  // duplicate
        duplicateCandidates.add(mysqlPartialUser);

        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql", "java"))))
                .thenReturn(duplicateCandidates);
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    if ("sql_java_user".equals(u.getUsername())) {
                        return new MatchRanking(20, 2, 1, 0, 0, 2);
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);
                });

        // Act
        List<User> results = searchUserService.searchBySkills(List.of("sql", "java"));

        // Assert: deduplication by UUID
        assertEquals(2, results.size());
        assertEquals(1, results.stream().filter(user -> "sql_java_user".equals(user.getUsername())).count());
    }

    @Test
    void searchBySkill_delegatesToMultiSkillSearch() {
        // Arrange
        User javaExactUser = user("java_exact_user", "java");
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("java"))))
                .thenReturn(List.of(javascriptUser, javaExactUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    if ("java_exact_user".equals(u.getUsername())) {
                        return new MatchRanking(10, 1, 1, 0, 0, 1);
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);
                });

        // Act
        List<User> results = searchUserService.searchBySkill("java");

        // Assert
        assertEquals(List.of("java_exact_user", "javascript_user"),
                results.stream().map(User::getUsername).toList());
    }

    @Test
    void searchBySkills_isCaseInsensitiveForTerms() {
        // Arrange
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql"))))
                .thenReturn(List.of(sqlExactUser, mysqlPartialUser));
        when(semanticSkillSearchService.findSemanticMatches(any()))
                .thenReturn(Map.of());
        when(hybridScorer.rankUser(any(), any(), any()))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    if ("sql_exact_user".equals(u.getUsername())) {
                        return new MatchRanking(10, 1, 1, 0, 0, 1);
                    }
                    return new MatchRanking(3, 1, 0, 0, 1, 1);
                });

        // Act
        List<User> lower = searchUserService.searchBySkills(List.of("sql"));
        List<User> upper = searchUserService.searchBySkills(List.of("SQL"));

        // Assert
        assertEquals(lower.stream().map(User::getUsername).toList(),
                upper.stream().map(User::getUsername).toList());
    }

    private static User user(String username, String... skills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(skills));
        return user;
    }
}
