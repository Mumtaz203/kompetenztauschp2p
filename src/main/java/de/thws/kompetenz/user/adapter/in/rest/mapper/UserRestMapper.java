package de.thws.kompetenz.user.adapter.in.rest.mapper;

import de.thws.kompetenz.user.adapter.in.rest.dto.profile.UpdateProfileResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

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
}