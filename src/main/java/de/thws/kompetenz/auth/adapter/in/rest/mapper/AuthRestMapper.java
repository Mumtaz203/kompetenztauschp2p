package de.thws.kompetenz.auth.adapter.in.rest.mapper;

import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterRequest;
import de.thws.kompetenz.auth.adapter.in.rest.dto.RegisterResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthRestMapper {

    /*
    To convert external input into the internal model
     */
    public User toDomain(RegisterRequest request){
        return new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
    }

    /*
    To control what we expose to the outside world
     */
    public RegisterResponse toResponse(User user){
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

    }




}
