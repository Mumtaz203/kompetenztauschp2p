package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.internal.http.HttpResponseException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test-user", roles = {"USER", "ADMIN"})
class UserResourceTest {

    @InjectMock
    UserRepositoryPort userRepositoryPort;

    @InjectMock
    IGetRatingSummaryUseCase getRatingSummaryUseCase;

    @Test
    void getAllUsers_returns200_withUsersAndWithoutPasswordField() {
        User first = user("alice", "java", "docker");
        User second = user("bob", "flutter");

        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(first, second));

        when(getRatingSummaryUseCase.getRatingSummaryForUser(any(UUID.class)))
                .thenReturn(new RatingSummary(BigDecimal.ZERO, 0));

        String body = given()
                .when().get("/users/getAllUsers")
                .then()
                .statusCode(200)
                .body("users", hasSize(2))
                .body("users.username", hasItems("alice", "bob"))
                .body("users[0].offeredSkills", notNullValue())
                .body("users[0].wantedSkills", notNullValue())
                .body("users[0].averagePoints", notNullValue())
                .body("users[0].ratingCount", equalTo(0))
                .extract()
                .asString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"));
    }

    @Test
    void getUserById_returns200_whenFound_andDoesNotExposePassword() {
        User user = user("alice", "java");

        when(userRepositoryPort.findUserById(user.getId())).thenReturn(Optional.of(user));

        when(getRatingSummaryUseCase.getRatingSummaryForUser(user.getId()))
                .thenReturn(new RatingSummary(BigDecimal.valueOf(4.5), 2));

        String body = given()
                .when().get("/users/getUser/{id}", user.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(user.getId().toString()))
                .body("username", equalTo("alice"))
                .body("email", equalTo("alice@test.com"))
                .body("averagePoints", notNullValue())
                .body("ratingCount", equalTo(2))
                .extract()
                .asString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"));
    }

    @Test
    void getUserById_returns404_whenMissing() {
        UUID id = UUID.randomUUID();

        when(userRepositoryPort.findUserById(id)).thenReturn(Optional.empty());

        given().when().get("/users/getUser/{id}", id).then().statusCode(404);
    }

    private static User user(String username, String... offeredSkills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(offeredSkills));
        user.setWantedSkills(List.of("spring"));
        return user;
    }
}