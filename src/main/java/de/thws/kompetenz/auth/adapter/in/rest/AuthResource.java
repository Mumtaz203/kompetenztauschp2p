package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.*;
import de.thws.kompetenz.auth.adapter.in.rest.mapper.AuthRestMapper;
import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.command.RegisterCommand;
import de.thws.kompetenz.auth.application.port.in.IGetCurrentUserUseCase;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.auth.application.result.LoginResult;
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
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Set;
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

    @ConfigProperty(name = "app.admin.id", defaultValue = "00000000-0000-0000-0000-000000000001")
    String adminId;

    @ConfigProperty(name = "app.admin.email")
    String adminEmail;

    @ConfigProperty(name = "app.admin.username", defaultValue = "admin")
    String adminUsername;

    @Inject
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
    public Response login(@Valid LoginRequest request) {

        LoginCommand loginCommand = authRestMapper.toLoginCommand(request);
        LoginResult result = loginUseCase.login(loginCommand);

        LoginResponse response = authRestMapper.toLoginResponse(result.token(), result.role());

        return Response.ok(response).build();
    }

    @GET
    @Path("/test")
    @Authenticated
    public String test() {
        return "secured";
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response getCurrentUser() {
        String userId = jwt.getSubject();
        Set<String> groups = jwt.getGroups();
        if (groups.contains("ADMIN") && adminId.equals(userId)) {
            CurrentUserReponse response = new CurrentUserReponse(
                    UUID.fromString(adminId),
                    adminUsername,
                    adminEmail
            );
            return Response.ok(response).build();
        }

        User user = getCurrentUserUseCase.getCurrentUser(UUID.fromString(userId));

        CurrentUserReponse response = authRestMapper.toCurrentUserResponse(user);

        return Response.ok(response).build();
    }
}
