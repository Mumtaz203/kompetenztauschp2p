package de.thws.kompetenz.auth.adapter.in.rest.mapper;

import de.thws.kompetenz.auth.adapter.in.rest.dto.*;
import de.thws.kompetenz.auth.application.command.LoginCommand;
import de.thws.kompetenz.auth.application.command.RegisterCommand;
import de.thws.kompetenz.auth.application.result.RegisterResult;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuthRestMapper {
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    /*
    To convert external input into the internal model
     */
    public RegisterCommand toRegisterCommand(RegisterRequest request){ //accessing domaın from adapter ask the proffessor about this
        return new RegisterCommand(
                request.username(),
                request.email(),
                request.password()
        );
    }

    /*
    To control what we expose to the outside world
     */
    public RegisterResponse toRegisterResponse(RegisterResult result){
        User user = result.user();
        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                result.token(),
                TOKEN_TYPE_BEARER
        );
    }

    public LoginCommand toLoginCommand(LoginRequest request){
        return new LoginCommand(
                request.email(),
                request.password()
        );
    }

    public LoginResponse toLoginResponse(String token,String role){//added role(admin or user)
        return new LoginResponse(token, TOKEN_TYPE_BEARER,role);
    }

    public CurrentUserReponse toCurrentUserResponse(User user){
        return new CurrentUserReponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }




}
