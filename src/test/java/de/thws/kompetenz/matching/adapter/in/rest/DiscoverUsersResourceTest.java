package de.thws.kompetenz.matching.adapter.in.rest;

import de.thws.kompetenz.matching.application.service.DiscoverUsersService;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendation;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test-user", roles = "USER")
class DiscoverUsersResourceTest {

    @InjectMock
    DiscoverUsersService discoverUsersService;

    @Test
    void discoverUsers_returnsRecommendationsAsJson() {
        UUID currentUserId = UUID.randomUUID();
        UUID recommendedUserId = UUID.randomUUID();
        when(discoverUsersService.recommendUsers(eq(currentUserId))).thenReturn(List.of(
                new UserRecommendation(
                        recommendedUserId,
                        "backend_user",
                        null,
                        null,
                        87,
                        0.91,
                        List.of("Java", "Spring Boot"),
                        "Your wanted skills match this user's offered skills."
                )
        ));

        given()
                .when()
                .get("/users/{userId}/discover", currentUserId)
                .then()
                .statusCode(200)
                .body("[0].userId", equalTo(recommendedUserId.toString()))
                .body("[0].username", equalTo("backend_user"))
                .body("[0].score", equalTo(87))
                .body("[0].bestSimilarity", equalTo(0.91f))
                .body("[0].matchedSkills", hasItem("Java"))
                .body("[0].matchedSkills", hasItem("Spring Boot"))
                .body("[0].matchReason", equalTo("Your wanted skills match this user's offered skills."));
    }

    @Test
    void discoverUsers_returnsEmptyList_whenServiceReturnsEmptyList() {
        UUID currentUserId = UUID.randomUUID();
        when(discoverUsersService.recommendUsers(eq(currentUserId))).thenReturn(List.of());

        given()
                .when()
                .get("/users/{userId}/discover", currentUserId)
                .then()
                .statusCode(200)
                .body("", empty());
    }

    @Test
    void discoverUsers_callsServiceWithCorrectUserId() {
        UUID currentUserId = UUID.randomUUID();
        when(discoverUsersService.recommendUsers(eq(currentUserId))).thenReturn(List.of());

        given()
                .when()
                .get("/users/{userId}/discover", currentUserId)
                .then()
                .statusCode(200);

        verify(discoverUsersService).recommendUsers(eq(currentUserId));
    }

    @Test
    void discoverUsers_responseDoesNotExposeRawEmbeddings() {
        UUID currentUserId = UUID.randomUUID();
        UUID recommendedUserId = UUID.randomUUID();
        when(discoverUsersService.recommendUsers(eq(currentUserId))).thenReturn(List.of(
                new UserRecommendation(
                        recommendedUserId,
                        "backend_user",
                        null,
                        null,
                        87,
                        0.91,
                        List.of("Java"),
                        "Your wanted skills match this user's offered skills."
                )
        ));

        String body = given()
                .when()
                .get("/users/{userId}/discover", currentUserId)
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertFalse(body.contains("embedding"));
        assertFalse(body.contains("embeddings"));
    }
}
