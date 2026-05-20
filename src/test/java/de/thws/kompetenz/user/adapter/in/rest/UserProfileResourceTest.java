package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.internal.http.HttpResponseException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserProfileResourceTest {

    @InjectMock
    UserRepositoryPort userRepositoryPort;

    @Test
    void getProfile_returns200_whenUserFound() {
        UUID userId = UUID.randomUUID();
        User user = createUser("cihan", userId);

        when(userRepositoryPort.findUserById(userId)).thenReturn(Optional.of(user));

        given()
                .when()
                .get("/users/{id}/profile", userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.toString()))
                .body("username", equalTo("cihan"))
                .body("email", equalTo("cihan@test.com"))
                .body("offeredSkills", notNullValue())
                .body("wantedSkills", notNullValue())
                .body("offeredSkills.size()", equalTo(2))
                .body("wantedSkills.size()", equalTo(1));
    }

    @Test
    void getProfile_returns200_withEmptySkillsWhenNoSkills() {
        UUID userId = UUID.randomUUID();

        User user = new User(userId, "alice", "alice@test.com", "secret");
        user.setOfferedSkills(List.of());
        user.setWantedSkills(List.of());

        when(userRepositoryPort.findUserById(userId)).thenReturn(Optional.of(user));

        given()
                .when()
                .get("/users/{id}/profile", userId)
                .then()
                .statusCode(200)
                .body("id", equalTo(userId.toString()))
                .body("username", equalTo("alice"))
                .body("email", equalTo("alice@test.com"))
                .body("offeredSkills.size()", equalTo(0))
                .body("wantedSkills.size()", equalTo(0));
    }

    @Test
    void getProfile_returns404_whenUserNotFound() {
        UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        when(userRepositoryPort.findUserById(userId)).thenReturn(Optional.empty());

        HttpResponseException exception = assertThrows(
                HttpResponseException.class,
                () -> given()
                        .when()
                        .get("/users/{id}/profile", userId)
        );

        assertTrue(exception.getMessage().contains("404"));
    }

    @Test
    void getProfile_doesNotExposePassword() {
        UUID userId = UUID.randomUUID();
        User user = createUser("bob", userId);

        when(userRepositoryPort.findUserById(userId)).thenReturn(Optional.of(user));

        String body = given()
                .when()
                .get("/users/{id}/profile", userId)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertFalse(body.contains("password"));
        assertFalse(body.contains("secret"));
    }

    private static User createUser(String username, UUID userId) {
        User user = new User(userId, username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of("java", "docker"));
        user.setWantedSkills(List.of("spring"));
        return user;
    }
}