package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import io.restassured.internal.http.HttpResponseException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class UserResourceTest {

    @InjectMock
    UserRepositoryPort userRepositoryPort;

    @Test
    void getAllUsers_returns200_withUsersAndWithoutPasswordField() {
        User first = user("alice", "java", "docker");
        User second = user("bob", "flutter");

        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(first, second));

        String body = given()
                .when().get("/users/getAllUsers")
                .then()
                .statusCode(200)
                .body("users", hasSize(2))
                .body("users.username", hasItems("alice", "bob"))
                .body("users[0].offeredSkills", notNullValue())
                .body("users[0].wantedSkills", notNullValue())
                .extract()
                .asString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"));
    }

    @Test
    void getUserById_returns200_whenFound_andDoesNotExposePassword() {
        User user = user("alice", "java");
        when(userRepositoryPort.findUserById(user.getId())).thenReturn(Optional.of(user));

        String body = given()
                .when().get("/users/getUser/{id}", user.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(user.getId().toString()))
                .body("username", equalTo("alice"))
                .body("email", equalTo("alice@test.com"))
                .extract()
                .asString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"));
    }

    @Test
    void getUserById_returns404_whenMissing() {
        UUID id = UUID.randomUUID();
        when(userRepositoryPort.findUserById(id)).thenReturn(Optional.empty());

        HttpResponseException ex = assertThrows(HttpResponseException.class,
                () -> given().when().get("/users/getUser/{id}", id));
        assertTrue(ex.getMessage().contains("status code: 404"));
    }

    private static User user(String username, String... offeredSkills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(offeredSkills));
        user.setWantedSkills(List.of("spring"));
        return user;
    }
}
