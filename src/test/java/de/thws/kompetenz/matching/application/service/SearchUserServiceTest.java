package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchUserServiceTest {

    @Mock
    UserRepositoryPort userRepositoryPort;

    SearchUserService searchUserService;

    private User sqlExactUser;
    private User mysqlPartialUser;
    private User postgresPartialUser;
    private User sqlJavaUser;
    private User javascriptUser;

    @BeforeEach
    void setUp() {
        searchUserService = new SearchUserService(userRepositoryPort);
        sqlExactUser = user("sql_exact_user", "sql");
        mysqlPartialUser = user("mysql_partial_user", "mysql");
        postgresPartialUser = user("postgres_partial_user", "postgresql");
        sqlJavaUser = user("sql_java_user", "sql", "java");
        javascriptUser = user("javascript_user", "javascript");
    }

    @Test
    void searchBySkills_returnsExactMatchesBeforePartialMatches() {
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql"))))
                .thenReturn(List.of(mysqlPartialUser, postgresPartialUser, sqlExactUser));

        List<User> results = searchUserService.searchBySkills(List.of("sql"));

        assertEquals(List.of("sql_exact_user", "mysql_partial_user", "postgres_partial_user"),
                results.stream().map(User::getUsername).toList());
    }

    @Test
    void searchBySkills_sortsByRelevanceForMultipleTerms() {
        User mysqlJavaUser = user("mysql_java_user", "mysql", "java");
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql", "java"))))
                .thenReturn(List.of(javascriptUser, mysqlJavaUser, sqlJavaUser, sqlExactUser));

        List<User> results = searchUserService.searchBySkills(List.of("sql", "java"));

        assertEquals("sql_java_user", results.getFirst().getUsername());
        assertTrue(results.indexOf(mysqlJavaUser) < results.indexOf(javascriptUser));
        assertTrue(results.indexOf(sqlExactUser) < results.indexOf(javascriptUser));
    }

    @Test
    void searchBySkills_removesDuplicateUsers() {
        List<User> duplicateCandidates = new ArrayList<>();
        duplicateCandidates.add(sqlJavaUser);
        duplicateCandidates.add(sqlJavaUser);
        duplicateCandidates.add(mysqlPartialUser);

        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql", "java"))))
                .thenReturn(duplicateCandidates);

        List<User> results = searchUserService.searchBySkills(List.of("sql", "java"));

        assertEquals(2, results.size());
        assertEquals(1, results.stream().filter(user -> "sql_java_user".equals(user.getUsername())).count());
    }

    @Test
    void searchBySkill_delegatesToMultiSkillSearch() {
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("java"))))
                .thenReturn(List.of(javascriptUser, user("java_exact_user", "java")));

        List<User> results = searchUserService.searchBySkill("java");

        assertEquals(List.of("java_exact_user", "javascript_user"),
                results.stream().map(User::getUsername).toList());
    }

    @Test
    void searchBySkills_breaksEqualScoresUsingMatchAndOfferedSkillTieBreakers() {
        User javaExactUser = user("java_exact_user", "java");
        User mysqlUser = user("mysql_partial_user", "mysql");
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("mysql", "java"))))
                .thenReturn(List.of(javaExactUser, mysqlUser, sqlJavaUser));

        List<User> results = searchUserService.searchBySkills(List.of("mysql", "java"));

        assertEquals(
                List.of("sql_java_user", "java_exact_user", "mysql_partial_user"),
                results.stream().map(User::getUsername).toList());
    }

    @Test
    void searchBySkills_prefersMoreMatchedTermsWhenScoresAreEqual() {
        User javaExactUser = user("java_exact_user", "java");
        User mysqlJavaUser = user("mysql_java_user", "mysql", "java");
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("mysql", "java"))))
                .thenReturn(List.of(javaExactUser, mysqlJavaUser));

        List<User> results = searchUserService.searchBySkills(List.of("mysql", "java"));

        assertEquals("mysql_java_user", results.getFirst().getUsername());
    }

    @Test
    void searchBySkills_isCaseInsensitiveForTerms() {
        when(userRepositoryPort.findCandidatesByOfferedSkills(eq(List.of("sql"))))
                .thenReturn(List.of(sqlExactUser, mysqlPartialUser));

        List<User> lower = searchUserService.searchBySkills(List.of("sql"));
        List<User> upper = searchUserService.searchBySkills(List.of("SQL"));

        assertEquals(lower.stream().map(User::getUsername).toList(),
                upper.stream().map(User::getUsername).toList());
    }

    private static User user(String username, String... skills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(skills));
        return user;
    }
}
