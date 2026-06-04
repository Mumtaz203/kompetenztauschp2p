package de.thws.kompetenz.user.adapter.in.rest.mapper;

import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateProfileResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetAllUsersResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.GetUserResponse;
import de.thws.kompetenz.user.adapter.in.rest.dto.user.UserResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserRestMapper {

    public UpdateProfileResponse toUpdateProfileResponse(User user) {
        if (user == null) return null;
        return new UpdateProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills()
        );
    }

    public GetUserResponse toGetUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new GetUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills()
        );
    }

    public GetAllUsersResponse toGetAllUsersResponse(List<User> users) {
        List<GetUserResponse> mappedUsers = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                GetUserResponse mapped = toGetUserResponse(user);
                if (mapped != null) {
                    mappedUsers.add(mapped);
                }
            }
        }
        return new GetAllUsersResponse(mappedUsers);
    }
    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getOfferedSkills(), user.getWantedSkills(),user.getPassword());
    }
}