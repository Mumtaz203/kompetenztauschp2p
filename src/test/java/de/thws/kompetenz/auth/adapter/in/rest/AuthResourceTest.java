package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.*;
import de.thws.kompetenz.auth.adapter.in.rest.mapper.AuthRestMapper;
import de.thws.kompetenz.auth.application.port.in.IGetCurrentUserUseCase;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@QuarkusTest
class AuthResourceTest {

    @InjectMock
    RegisterUseCase registerUseCase;

    @InjectMock
    ILoginUseCase loginUseCase;

    @InjectMock
    IGetCurrentUserUseCase getCurrentUserUseCase;

    @InjectMock
    AuthRestMapper authRestMapper;

    @InjectMock
    JsonWebToken jwt;

    @Inject
    AuthResource authResource;     // Real CDI bean

    @BeforeEach
    void setup() {
        reset(registerUseCase, loginUseCase, getCurrentUserUseCase, authRestMapper, jwt);
    }

    @Test
    void testRegister() {
        RegisterRequest request = new RegisterRequest(
                "myusername",
                "user@example.com",
                "mysecurepassword"
        );

        RegisterResponse expectedResponse = new RegisterResponse(
                UUID.randomUUID(),
                "myusername",
                "user@example.com",
                "fake-jwt-token",
                "Bearer"
        );

        when(authRestMapper.toRegisterCommand(any(RegisterRequest.class))).thenReturn(mock());
        when(registerUseCase.register(any())).thenReturn(mock());
        when(authRestMapper.toRegisterResponse(any())).thenReturn(expectedResponse);

        var response = authResource.register(request);

        assertEquals(201, response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
    }

    @Test
    void testLogin() {
        LoginRequest request = new LoginRequest("user@example.com", "mysecurepassword");

        LoginResponse expectedResponse = new LoginResponse("fake-jwt-token", "Bearer");

        when(authRestMapper.toLoginCommand(any())).thenReturn(mock());
        when(loginUseCase.login(any())).thenReturn("fake-jwt-token");
        when(authRestMapper.toLoginResponse(anyString())).thenReturn(expectedResponse);

        var response = authResource.login(request);

        assertEquals(200, response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
    }

    @Test
    @TestSecurity(user = "test-user", roles = "user")
    void testGetCurrentUser() {
        UUID userId = UUID.randomUUID();
        User user = mock(User.class);

        CurrentUserReponse expectedResponse = new CurrentUserReponse(   // Fixed typo
                userId,
                "myusername",
                "user@example.com"
        );

        when(jwt.getSubject()).thenReturn(userId.toString());
        when(getCurrentUserUseCase.getCurrentUser(userId)).thenReturn(user);
        when(authRestMapper.toCurrentUserResponse(user)).thenReturn(expectedResponse);

        var response = authResource.getCurrentUser();

        assertEquals(200, response.getStatus());
        assertEquals(expectedResponse, response.getEntity());
    }

    @Test
    @TestSecurity(user = "test-user", roles = "user")   // or whatever roles you need
    void testSecuredEndpoint() {
        String result = authResource.test();
        assertEquals("secured", result);
    }
}