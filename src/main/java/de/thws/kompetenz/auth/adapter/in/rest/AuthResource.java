package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.*;
import de.thws.kompetenz.auth.adapter.in.rest.mapper.AuthRestMapper;
import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.command.RegisterCommand;
import de.thws.kompetenz.auth.application.port.in.IGetCurrentUserUseCase;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.auth.application.result.RegisterResult;
import de.thws.kompetenz.user.domain.model.User;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
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
    @PermitAll
    public Response register(@Valid RegisterRequest request) {

        RegisterCommand registerCommand = authRestMapper.toRegisterCommand(request);

        RegisterResult result = registerUseCase.register(registerCommand);

        RegisterResponse response = authRestMapper.toRegisterResponse(result);

        return Response.status(Response.Status.CREATED).
                entity(response).
                build();
    }

    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) { /* would ıt be cleaner to use a LogınCommand class and a mapper to thıs class ınstead to map from user
                                                            to request*/

        LoginCommand loginCommand = authRestMapper.toLoginCommand(request);
        String token = loginUseCase.login(loginCommand);

        LoginResponse response = authRestMapper.toLoginResponse(token);

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

        CurrentUserReponse response = authRestMapper.toCurrentUserResponse(user);

        return Response.ok(response).build();
    }
}
