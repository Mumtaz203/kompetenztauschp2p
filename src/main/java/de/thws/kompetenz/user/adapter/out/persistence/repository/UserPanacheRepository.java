package de.thws.kompetenz.user.adapter.out.persistence.repository;

import de.thws.kompetenz.user.adapter.out.persistence.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserPanacheRepository implements PanacheRepository<UserEntity> {

    public Optional<UserEntity> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

    public Optional<UserEntity> findUserById(UUID userId) {
        return find("id", userId).firstResultOptional();
    }

    public List<UserEntity> findUsersBySkill(String skill) {
        if (skill == null || skill.isBlank()) {
            return List.of();
        }

        String normalized = skill.trim().toLowerCase();
        if (normalized.length() < 3) {
            return List.of();
        }

        return find("""
            select distinct u
            from UserEntity u
            join u.offeredSkills os
            where lower(os) = ?1
               or lower(os) like ?2
            """, normalized, normalized + "%").list();
    }
}
