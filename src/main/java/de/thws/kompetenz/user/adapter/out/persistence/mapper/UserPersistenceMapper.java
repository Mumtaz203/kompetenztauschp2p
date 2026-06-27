package de.thws.kompetenz.user.adapter.out.persistence.mapper;

import de.thws.kompetenz.user.adapter.out.persistence.entity.UserEntity;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class UserPersistenceMapper {

    public UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity();
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setProfileImageUrl(user.getProfileImageUrl());
        entity.setUniversity(user.getUniversity());
        entity.setOfferedSkills(copy(user.getOfferedSkills()));
        entity.setWantedSkills(copy(user.getWantedSkills()));
        entity.setPrivateReportCount(user.getPrivateReportCount());
        entity.setInternallyFlagged(user.isInternallyFlagged());

        return entity;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user= new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword()
        );
        user.setProfileImageUrl(entity.getProfileImageUrl());
        user.setUniversity(entity.getUniversity());
        user.setOfferedSkills(copy(entity.getOfferedSkills()));
        user.setWantedSkills(copy(entity.getWantedSkills()));
        user.setPrivateReportCount(entity.getPrivateReportCount());
        user.setInternallyFlagged(entity.isInternallyFlagged());
        return user;
    }
    private List<String> copy(List<String> in) {
        return in == null ? new ArrayList<>() : new ArrayList<>(in);
    }
}
