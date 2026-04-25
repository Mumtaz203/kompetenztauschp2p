package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.LoginRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.LoginResponse;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterResponse;
import de.thws.kompetenz.auth.adapter.in.rest.mapper.AuthRestMapper;
import de.thws.kompetenz.auth.application.port.in.ILoginUseCase;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final RegisterUseCase registerUseCase;
    private final AuthRestMapper authRestMapper;

    private final ILoginUseCase loginUseCase;


    public AuthResource(RegisterUseCase registerUseCase, AuthRestMapper authRestMapper,
                        ILoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.authRestMapper = authRestMapper;
        this.loginUseCase = loginUseCase;
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
    public Response login(@Valid LoginRequest request) {

        String token = loginUseCase.login(
                request.getEmail(),
                request.getPassword()
        );

        LoginResponse response = new LoginResponse(token);

        return Response.ok(response).build();
    }
}
