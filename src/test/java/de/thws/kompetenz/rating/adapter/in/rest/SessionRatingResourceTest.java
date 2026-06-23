package de.thws.kompetenz.rating.adapter.in.rest;

import de.thws.kompetenz.rating.adapter.in.rest.dto.SessionRatingResponse;
import de.thws.kompetenz.rating.adapter.in.rest.mapper.SessionRatingMapper;
import de.thws.kompetenz.rating.application.exception.SessionRatingNotFoundException;
import de.thws.kompetenz.rating.application.in.ICreateSessionRatingUseCase;
import de.thws.kompetenz.rating.application.in.IGetRatingSummaryUseCase;
import de.thws.kompetenz.rating.application.in.IGetUserRatingUseCase;
import de.thws.kompetenz.rating.application.in.IPublishSessionRatingsUseCase;
import de.thws.kompetenz.rating.domain.RatingStatus;
import de.thws.kompetenz.rating.domain.SessionRating;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static de.thws.kompetenz.common.RestAssuredStatusAssert.assertStatus;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestSecurity(user = "test-user", roles = "USER")
class SessionRatingResourceTest {

    @InjectMock
    ICreateSessionRatingUseCase createSessionRatingUseCase;

    @InjectMock
    SessionRatingMapper mapper;

    @InjectMock
    IPublishSessionRatingsUseCase publishSessionRatingsUseCase;

    @InjectMock
    IGetRatingSummaryUseCase getRatingSummaryUseCase;

    @InjectMock
    IGetUserRatingUseCase getUserRatingUseCase;

    @InjectMock
    JsonWebToken jwt;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        Mockito.when(jwt.getSubject()).thenReturn(currentUserId.toString());
    }

    @Test
    void getRatingsForUser_returnsPublishedRatingsForSelf() {
        UUID ratingId = UUID.randomUUID();
        SessionRating rating = new SessionRating(
                ratingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                currentUserId,
                RatingStatus.PUBLISHED,
                BigDecimal.valueOf(4.5),
                "Helpful exchange",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );
        SessionRatingResponse response = new SessionRatingResponse(
                ratingId,
                rating.getSessionId(),
                rating.getSenderUserId(),
                rating.getReceiverUserId(),
                rating.getStatus(),
                rating.getPoints(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getPublishedAt()
        );

        when(getUserRatingUseCase.getPublishedRatingsForUser(currentUserId)).thenReturn(List.of(rating));
        when(mapper.toResponse(rating)).thenReturn(response);

        given()
                .when().get("/ratings/users/{userId}", currentUserId)
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(ratingId.toString()))
                .body("[0].receiverUserId", equalTo(currentUserId.toString()));
    }

    @Test
    void getRatingsForUser_returns403_forOtherUser() {
        assertStatus(403, () -> given()
                .when().get("/ratings/users/{userId}", UUID.randomUUID())
                .then()
                .statusCode(403));
    }

    @Test
    void getRatingById_returnsRatingForReceiver() {
        UUID ratingId = UUID.randomUUID();
        SessionRating rating = new SessionRating(
                ratingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                currentUserId,
                RatingStatus.PUBLISHED,
                BigDecimal.valueOf(5.0),
                "Great session",
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2)
        );
        SessionRatingResponse response = new SessionRatingResponse(
                ratingId,
                rating.getSessionId(),
                rating.getSenderUserId(),
                rating.getReceiverUserId(),
                rating.getStatus(),
                rating.getPoints(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getPublishedAt()
        );

        when(getUserRatingUseCase.getRating(ratingId)).thenReturn(rating);
        when(mapper.toResponse(rating)).thenReturn(response);

        given()
                .when().get("/ratings/{ratingId}", ratingId)
                .then()
                .statusCode(200)
                .body("id", equalTo(ratingId.toString()))
                .body("receiverUserId", equalTo(currentUserId.toString()));
    }

    @Test
    void getRatingById_returns403_whenCurrentUserIsNotReceiver() {
        UUID ratingId = UUID.randomUUID();
        SessionRating rating = new SessionRating(
                ratingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                RatingStatus.PUBLISHED,
                BigDecimal.valueOf(4.0),
                "Private feedback",
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        when(getUserRatingUseCase.getRating(ratingId)).thenReturn(rating);

        assertStatus(403, () -> given()
                .when().get("/ratings/{ratingId}", ratingId)
                .then()
                .statusCode(403));
    }

    @Test
    void getRatingById_returns404_whenMissing() {
        UUID ratingId = UUID.randomUUID();

        when(getUserRatingUseCase.getRating(ratingId))
                .thenThrow(new SessionRatingNotFoundException(ratingId));

        assertStatus(404, () -> given()
                .when().get("/ratings/{ratingId}", ratingId)
                .then()
                .statusCode(404));
    }

    @Test
    void getRatingById_returns403_forReceiverWhenRatingIsNotPublished() {
        UUID ratingId = UUID.randomUUID();
        SessionRating rating = new SessionRating(
                ratingId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                currentUserId,
                RatingStatus.PENDING,
                BigDecimal.valueOf(4.0),
                "Hidden until publish",
                LocalDateTime.now().minusDays(2),
                null
        );

        when(getUserRatingUseCase.getRating(ratingId)).thenReturn(rating);

        assertStatus(403, () -> given()
                .when().get("/ratings/{ratingId}", ratingId)
                .then()
                .statusCode(403));
    }
}
