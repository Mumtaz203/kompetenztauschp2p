package de.thws.kompetenz.matching.adapter.in.rest.mapper;

import de.thws.kompetenz.matching.adapter.in.rest.dto.SearchUserResponse;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
                toResponseSkills(user.getOfferedSkills()),
                toResponseSkills(user.getWantedSkills())
        );
    }

    private List<String> toResponseSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        Set<String> uniqueSkills = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill == null || skill.isBlank()) {
                continue;
            }
            uniqueSkills.add(skill.trim());
        }
        return List.copyOf(uniqueSkills);
    }
}
