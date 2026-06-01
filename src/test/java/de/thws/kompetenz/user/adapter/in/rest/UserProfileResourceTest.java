package de.thws.kompetenz.user.adapter.in.rest;

import de.thws.kompetenz.user.adapter.in.rest.dto.profile.*;
import de.thws.kompetenz.user.adapter.in.rest.mapper.UserRestMapper;
import de.thws.kompetenz.user.application.port.in.IGetUserByIdUseCase;
import de.thws.kompetenz.user.application.port.in.UpdateUserProfileUseCase;
import de.thws.kompetenz.user.domain.model.User;
import de.thws.kompetenz.user.domain.model.exception.UserNotFoundException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class UserProfileResourceTest {

    @InjectMock
    UpdateUserProfileUseCase updateUserProfileUseCase;

    @InjectMock
    UserRestMapper userRestMapper;

    @InjectMock
    IGetUserByIdUseCase iGetUserByIdUseCase;

    UserProfileResource resource;

    private final UUID userId = UUID.randomUUID();
    private User testUser;
    private UpdateProfileResponse successResponse;

    @BeforeEach
    void setup() {
        resource = new UserProfileResource(updateUserProfileUseCase, iGetUserByIdUseCase, userRestMapper);

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setOfferedSkills(List.of("Java"));
        testUser.setWantedSkills(List.of("Spring"));

        successResponse = new UpdateProfileResponse(
                userId, "testuser", "test@example.com",
                List.of("Java"), List.of("Spring")
        );

        reset(updateUserProfileUseCase, userRestMapper);
        when(userRestMapper.toUpdateProfileResponse(any(User.class))).thenReturn(successResponse);

        // Allow any UUID for all use case calls
        when(updateUserProfileUseCase.updateName(any(UUID.class), anyString())).thenReturn(testUser);
        when(updateUserProfileUseCase.updateUser(any(UUID.class), any(User.class))).thenReturn(testUser);
        when(updateUserProfileUseCase.updateSkills(any(UUID.class), anyList(), anyList())).thenReturn(testUser);
    }

    // --- updateName tests ---
    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateName_shouldReturn200() {
        UpdateNameRequest request = new UpdateNameRequest("newusername");

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateName", userId)
                .then()
                .statusCode(200)
                .body("username", equalTo("testuser"));
    }

    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateName_shouldReturn400_whenInvalid() {
        UpdateNameRequest request = new UpdateNameRequest(""); // invalid name

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateName", userId)
                .then()
                .statusCode(400);
    }


    // --- updateUser tests ---
    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateUser_shouldReturn200() {
        UpdateUserRequest request = new UpdateUserRequest(
                "newusername",
                List.of("Go"),
                List.of("Kafka")
        );

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateUser", userId)
                .then()
                .statusCode(200)
                .body("username", equalTo("testuser"));
    }

    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateUser_shouldReturn400_whenUsernameBlank() {
        UpdateUserRequest request = new UpdateUserRequest(
                " ",
                List.of("Go"),
                List.of("Kafka")
        );

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateUser", userId)
                .then()
                .statusCode(400);
    }


    // --- updateSkills tests ---
    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateSkills_shouldReturn200() {
        UpdateSkillsRequest request = new UpdateSkillsRequest(
                List.of("Java", "Quarkus"),
                List.of("Docker", "Kafka")
        );

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateSkills", userId)
                .then()
                .statusCode(200)
                .body("username", equalTo("testuser"));
    }


    // --- Sensitive data check ---
    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateSkills_shouldNotReturnSensitiveData() {
        UpdateSkillsRequest request = new UpdateSkillsRequest(
                List.of("Java"),
                List.of("Kafka")
        );
        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateSkills", userId)
                .then()
                .statusCode(200)
                .body("password", nullValue())
                .body("username", notNullValue())
                .body("email", notNullValue());
    }


    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateSkills_shouldHandleEmptyLists() {
        UpdateSkillsRequest request = new UpdateSkillsRequest(List.of(), List.of());

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateSkills", userId)
                .then()
                .statusCode(200)
                .body("offeredSkills.size()", equalTo(0))
                .body("wantedSkills.size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void updateUser_shouldReturn404_whenUserNotFound() {
        UpdateUserRequest request = new UpdateUserRequest(
                "newuser",
                List.of("Go"),
                List.of("Kafka")
        );

        UUID unknownUserId = UUID.randomUUID();

        // Mock the use case to throw when called with unknown userId
        when(updateUserProfileUseCase.updateUser(eq(unknownUserId), any(User.class)))
                .thenThrow(new UserNotFoundException(unknownUserId));

        given()
                .contentType("application/json")
                .body(request)
                .when()
                .put("/users/{id}/updateUser", unknownUserId)
                .then()
                .statusCode(404);
    }


}