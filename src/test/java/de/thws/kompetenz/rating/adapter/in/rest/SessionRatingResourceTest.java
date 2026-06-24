package de.thws.kompetenz.rating.adapter.in.rest;

import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.rating.application.in.ICreateSessionRatingUseCase;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.application.in.IGetUserRatingUseCase;
import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.rating.application.in.IUpdateSessionRatingStatusUseCase;
import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.RatingSummary;
import de.thws.kompetenz.rating.domain.SessionRating;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class SessionRatingResourceTest {

    private static final UUID SESSION_ID = UUID.randomUUID();
    private static final UUID RATING_ID = UUID.randomUUID();
    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final UUID RECEIVER_ID = UUID.randomUUID();

    @InjectMock
    ICreateSessionRatingUseCase createSessionRatingUseCase;

    @InjectMock
    IPublishSessionRatingsUseCase publishSessionRatingsUseCase;

    @InjectMock
    IGetRatingSummaryUseCase getRatingSummaryUseCase;

    @InjectMock
    IGetUserRatingUseCase getUserRatingUseCase;

    @InjectMock
    IUpdateSessionRatingStatusUseCase updateRatingStatusUseCase;

    @InjectMock
    AuthorizationGuard authorizationGuard;

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void createSessionRating_shouldUseCurrentUserAsSender() {
        SessionRating createdRating = pendingRating();

        when(authorizationGuard.currentUserId()).thenReturn(SENDER_ID);

        when(createSessionRatingUseCase.createRating(
                eq(SESSION_ID),
                eq(SENDER_ID),
                eq(RECEIVER_ID),
                any(BigDecimal.class),
                eq("Good session")
        )).thenReturn(createdRating);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "sessionId", SESSION_ID.toString(),
                        "receiverUserId", RECEIVER_ID.toString(),
                        "points", 4.5,
                        "comment", "Good session"
                ))
                .when()
                .post("/ratings/create/")
                .then()
                .statusCode(201);

        verify(authorizationGuard).currentUserId();

        verify(createSessionRatingUseCase).createRating(
                eq(SESSION_ID),
                eq(SENDER_ID),
                eq(RECEIVER_ID),
                any(BigDecimal.class),
                eq("Good session")
        );
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void publishRatingsForSession_shouldPublishAsAdmin() {
        SessionRating publishedRating = publishedRating();

        when(publishSessionRatingsUseCase.publishRatingsForSession(SESSION_ID))
                .thenReturn(List.of(publishedRating));

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/ratings/sessions/{sessionId}/publish", SESSION_ID)
                .then()
                .statusCode(200);

        verify(publishSessionRatingsUseCase).publishRatingsForSession(SESSION_ID);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void publishRatingsForSession_shouldBeForbiddenForNormalUser() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/ratings/sessions/{sessionId}/publish", SESSION_ID)
                .then()
                .statusCode(403);

        verifyNoInteractions(publishSessionRatingsUseCase);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void getRatingSummaryForUser_shouldRequireSelfOrAdminAndReturnSummary() {
        RatingSummary summary = new RatingSummary(BigDecimal.valueOf(4.5), 2);

        doNothing().when(authorizationGuard).requireSelfOrAdmin(RECEIVER_ID);
        when(getRatingSummaryUseCase.getRatingSummaryForUser(RECEIVER_ID))
                .thenReturn(summary);

        given()
                .when()
                .get("/ratings/users/{userId}/summary", RECEIVER_ID)
                .then()
                .statusCode(200);

        verify(authorizationGuard).requireSelfOrAdmin(RECEIVER_ID);
        verify(getRatingSummaryUseCase).getRatingSummaryForUser(RECEIVER_ID);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getPublishedRatingsForUser_shouldBeAvailableForAdmin() {
        when(getUserRatingUseCase.getPublishedRatingsForUser(RECEIVER_ID))
                .thenReturn(List.of(publishedRating()));

        given()
                .when()
                .get("/ratings/users/{userId}", RECEIVER_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getPublishedRatingsForUser(RECEIVER_ID);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void getPublishedRatingsForUser_shouldBeForbiddenForNormalUserBecauseEndpointIsAdminOnly() {
        given()
                .when()
                .get("/ratings/users/{userId}", RECEIVER_ID)
                .then()
                .statusCode(403);

        verifyNoInteractions(getUserRatingUseCase);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void getMyRatings_shouldUseCurrentUserIdAndCallGetOwnRatings() {
        when(authorizationGuard.currentUserId()).thenReturn(SENDER_ID);

        when(getUserRatingUseCase.getOwnRatings(SENDER_ID))
                .thenReturn(List.of(pendingRating()));

        given()
                .when()
                .get("/ratings/me")
                .then()
                .statusCode(200);

        verify(authorizationGuard).currentUserId();
        verify(getUserRatingUseCase).getOwnRatings(SENDER_ID);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void getVisibleRatingById_shouldPassCurrentUserAndAdminFlagToUseCase() {
        when(authorizationGuard.currentUserId()).thenReturn(SENDER_ID);
        when(authorizationGuard.isAdmin()).thenReturn(false);

        when(getUserRatingUseCase.getVisibleRating(RATING_ID, SENDER_ID, false))
                .thenReturn(pendingRating());

        given()
                .when()
                .get("/ratings/{ratingId}", RATING_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getVisibleRating(RATING_ID, SENDER_ID, false);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getAllRatings_shouldBeAvailableForAdmin() {
        when(getUserRatingUseCase.getAllRatings())
                .thenReturn(List.of(pendingRating(), publishedRating()));

        given()
                .when()
                .get("/ratings/get-all-ratings")
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getAllRatings();
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void getAllRatings_shouldBeForbiddenForNormalUser() {
        given()
                .when()
                .get("/ratings/get-all-ratings")
                .then()
                .statusCode(403);

        verifyNoInteractions(getUserRatingUseCase);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getAllPublishedRatings_shouldBeAvailableForAdmin() {
        when(getUserRatingUseCase.getAllPublishedRatings())
                .thenReturn(List.of(publishedRating()));

        given()
                .when()
                .get("/ratings/admin/published-ratings")
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getAllPublishedRatings();
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getAllNonPublishedRatings_shouldBeAvailableForAdmin() {
        when(getUserRatingUseCase.getAllNonPublishedRatings())
                .thenReturn(List.of(pendingRating()));

        given()
                .when()
                .get("/ratings/admin/non-published")
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getAllNonPublishedRatings();
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getAllRatingsForUser_shouldBeAvailableForAdmin() {
        when(getUserRatingUseCase.getAllRatingsForUser(RECEIVER_ID))
                .thenReturn(List.of(pendingRating(), publishedRating()));

        given()
                .when()
                .get("/ratings/admin/users/{userId}", RECEIVER_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getAllRatingsForUser(RECEIVER_ID);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getRatingByIdForAdmin_shouldReturnAnyRating() {
        when(getUserRatingUseCase.getRating(RATING_ID))
                .thenReturn(pendingRating());

        given()
                .when()
                .get("/ratings/admin/{ratingId}", RATING_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getRating(RATING_ID);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getPublishedRatingByIdForAdmin_shouldUsePublishedMethod() {
        when(getUserRatingUseCase.getPublishedRating(RATING_ID))
                .thenReturn(publishedRating());

        given()
                .when()
                .get("/ratings/admin/published/{ratingId}", RATING_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getPublishedRating(RATING_ID);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void getNonPublishedRatingByIdForAdmin_shouldUseNonPublishedMethod() {
        when(getUserRatingUseCase.getNonPublishedRating(RATING_ID))
                .thenReturn(pendingRating());

        given()
                .when()
                .get("/ratings/admin/non-published/{ratingId}", RATING_ID)
                .then()
                .statusCode(200);

        verify(getUserRatingUseCase).getNonPublishedRating(RATING_ID);
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    void updateRatingStatus_shouldBeAvailableForAdmin() {
        SessionRating updatedRating = publishedRating();

        when(updateRatingStatusUseCase.updateRatingStatus(RATING_ID, RatingStatus.PUBLISHED))
                .thenReturn(updatedRating);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("status", "PUBLISHED"))
                .when()
                .patch("/ratings/admin/{ratingId}/status", RATING_ID)
                .then()
                .statusCode(200);

        verify(updateRatingStatusUseCase)
                .updateRatingStatus(RATING_ID, RatingStatus.PUBLISHED);
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    void updateRatingStatus_shouldBeForbiddenForNormalUser() {
        given()
                .contentType(ContentType.JSON)
                .body(Map.of("status", "PENDING"))
                .when()
                .patch("/ratings/admin/{ratingId}/status", RATING_ID)
                .then()
                .statusCode(403);

        verifyNoInteractions(updateRatingStatusUseCase);
    }

    private SessionRating pendingRating() {
        return SessionRating.create(
                SESSION_ID,
                SENDER_ID,
                RECEIVER_ID,
                BigDecimal.valueOf(4.5),
                "Test rating"
        );
    }

    private SessionRating publishedRating() {
        SessionRating rating = SessionRating.create(
                SESSION_ID,
                SENDER_ID,
                RECEIVER_ID,
                BigDecimal.valueOf(4.5),
                "Test rating"
        );

        rating.publish(LocalDateTime.now());

        return rating;
    }
}