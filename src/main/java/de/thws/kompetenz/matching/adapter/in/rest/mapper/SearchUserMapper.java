package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SearchUserMapper {

    public SearchUserResponse toSearchUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return new SearchUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getOfferedSkills(),
                user.getWantedSkills()
        );
    }
}