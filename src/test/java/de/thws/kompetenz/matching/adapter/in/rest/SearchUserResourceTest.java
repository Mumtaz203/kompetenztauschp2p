package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static de.thws.kompetenz.common.RestAssuredStatusAssert.assertStatus;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test-user", roles = "USER")
class SearchUserResourceTest {

    @InjectMock
    SearchUserUseCase searchUserUseCase;

    @InjectMock
    EmbeddingClientPort embeddingClientPort;

    @Test
    void search_returns400_whenNoSearchTermsProvided() {

        assertStatus(400, () -> given()
                .when().get("/users/search")
                .then()
                .statusCode(400));

    }

    @Test
    void search_returns400_whenTermIsTooShort() {
        assertStatus(400, () -> given().queryParam("skills", "js")
                .when()
                .get("/users/search")
                .then()
                .statusCode(400));

    }

    @Test
    void search_returns400_whenSkillsParameterIsBlank() {


        assertStatus(400, () -> given()
                .when().get("/users/search")
                .then()
                .statusCode(400));

    }

    @Test
    void search_returns200_forLegacySkillParameter() {
        User user = searchUser("java_exact_user", "java");
        when(searchUserUseCase.searchBySkills(eq(List.of("java")))).thenReturn(List.of(user));

        given()
                .queryParam("skill", "java")
                .when().get("/users/search")
                .then()
                .statusCode(200)
                .body("username", hasItem("java_exact_user"));
    }

    @Test
    void search_returns200_forSkillsParameter() {
        User user = searchUser("sql_java_user", "sql", "java");
        when(searchUserUseCase.searchBySkills(eq(List.of("sql", "java")))).thenReturn(List.of(user));

        given()
                .queryParam("skills", "sql,java")
                .when().get("/users/search")
                .then()
                .statusCode(200)
                .body("username", hasItem("sql_java_user"));

        verify(searchUserUseCase).searchBySkills(eq(List.of("sql", "java")));
    }

    @Test
    void search_parsesWhitespaceSeparatedSkills() {
        when(searchUserUseCase.searchBySkills(eq(List.of("sql", "java")))).thenReturn(List.of());

        given()
                .queryParam("skills", "sql java")
                .when().get("/users/search")
                .then()
                .statusCode(200);

        verify(searchUserUseCase).searchBySkills(eq(List.of("sql", "java")));
    }

    @Test
    void search_normalizesCaseInsensitiveTermsBeforeUseCaseCall() {
        when(searchUserUseCase.searchBySkills(eq(List.of("sql")))).thenReturn(List.of());

        given()
                .queryParam("skills", "SQL")
                .when().get("/users/search")
                .then()
                .statusCode(200);

        verify(searchUserUseCase).searchBySkills(eq(List.of("sql")));
    }

    @Test
    void search_preservesServiceResultOrderInResponse() {
        User first = searchUser("sql_java_user", "sql", "java");
        User second = searchUser("mysql_partial_user", "mysql");
        when(searchUserUseCase.searchBySkills(eq(List.of("sql", "java"))))
                .thenReturn(List.of(first, second));

        given()
                .queryParam("skills", "sql,java")
                .when().get("/users/search")
                .then()
                .statusCode(200)
                .body("username[0]", equalTo("sql_java_user"))
                .body("username[1]", equalTo("mysql_partial_user"));
    }

    @Test
    void search_responseDoesNotExposeInternalScoringFields() {
        User user = searchUser("sql_exact_user", "sql");
        when(searchUserUseCase.searchBySkills(eq(List.of("sql")))).thenReturn(List.of(user));

        String body = given()
                .queryParam("skills", "sql")
                .when().get("/users/search")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("relevanceScore"));
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("scoredUser"));
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("\"score\""));
    }

    @Test
    void search_responseUsesArraysForSkillLists() {
        User user = searchUser("sql_exact_user", "sql");
        user.setWantedSkills(null);
        when(searchUserUseCase.searchBySkills(eq(List.of("sql")))).thenReturn(List.of(user));

        given()
                .queryParam("skills", "sql")
                .when().get("/users/search")
                .then()
                .statusCode(200)
                .body("[0].offeredSkills", hasItem("sql"))
                .body("[0].wantedSkills", empty());
    }

    private static User searchUser(String username, String... skills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(skills));
        user.setWantedSkills(List.of());
        return user;
    }
}
