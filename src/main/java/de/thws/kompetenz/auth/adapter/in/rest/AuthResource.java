package de.thws.kompetenz.auth.adapter.in.rest;

import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterResponse;
import de.thws.kompetenz.auth.application.port.in.RegisterUseCase;
import de.thws.kompetenz.auth.domain.model.User;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final RegisterUseCase registerUseCase;

    public AuthResource(RegisterUseCase registerUseCase) {
        this.registerUseCase = registerUseCase;
    }

    @POST
    @Path("/register")
    public Response register(@Valid RegisterRequest request) {
        User user = registerUseCase.register(request.getUsername(), request.getEmail(), request.getPassword());
        RegisterResponse response = new RegisterResponse(user.getId(), user.getUsername(), user.getEmail());
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
}
