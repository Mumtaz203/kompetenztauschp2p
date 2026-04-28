package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.LoginRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.LoginResponse;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterResponse;
import de.thws.kompetenz.auth.adapter.in.rest.mapper.AuthRestMapper;
import de.thws.kompetenz.auth.application.port.in.IGetCurrentUserUseCase;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

@ApplicationScoped
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final RegisterUseCase registerUseCase;
    private final AuthRestMapper authRestMapper;

    private final ILoginUseCase loginUseCase;

    private final IGetCurrentUserUseCase getCurrentUserUseCase;


    @Inject //aquivalent to a constructor injection like others below
    JsonWebToken jwt;


    public AuthResource(RegisterUseCase registerUseCase, AuthRestMapper authRestMapper,
                        ILoginUseCase loginUseCase, IGetCurrentUserUseCase getCurrentUserUseCase) {
        this.registerUseCase = registerUseCase;
        this.authRestMapper = authRestMapper;
        this.loginUseCase = loginUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {

        User newUser = authRestMapper.toDomain(request);

        User createdUser = registerUseCase.register(newUser);
        RegisterResponse response = authRestMapper.toResponse(createdUser);

        return Response.status(Response.Status.CREATED).
                entity(response).
                build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) { /* would ıt be cleaner to use a LogınCommand class and a mapper to thıs class ınstead to map from user
                                                            to request*/

        String token = loginUseCase.login(
                request.getEmail(),
                request.getPassword()
        );

        LoginResponse response = new LoginResponse(token);

        return Response.ok(response).build();
    }

    @GET
    @Path("/test")
    @Authenticated //when authenticated is used it automatically uses publicKey.pem
    public String test() {
        return "secured";
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentUser() {

        String userId = jwt.getSubject(); // comes from JWT

        User user = getCurrentUserUseCase.getCurrentUser(
                UUID.fromString(userId)
        );

        RegisterResponse response = authRestMapper.toResponse(user);

        return Response.ok(response).build();
    }
}
