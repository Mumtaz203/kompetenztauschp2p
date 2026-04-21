package de.thws.kompetenz.auth.adapter.out.persistence.mapper;

import de.thws.kompetenz.auth.adapter.out.persistence.entity.UserEntity;
import de.thws.kompetenz.auth.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserPersistenceMapper {

    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserEntity(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
        );
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword()
        );
    }
}
