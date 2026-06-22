package de.thws.kompetenz.user.adapter.out.persistence.repository;

import de.thws.kompetenz.user.adapter.out.persistence.entity.UserEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

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

    public List<UserEntity> findUsersByOfferedSkillsContainingAny(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }

        List<String> terms = skills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(skill -> skill.trim().toLowerCase(Locale.ROOT))
                .filter(term -> term.length() >= 3)
                .distinct()
                .toList();
        if (terms.isEmpty()) {
            return List.of();
        }

        if (terms.size() == 1) {
            String pattern = "%" + terms.getFirst() + "%";
            return find("""
                select distinct u
                from UserEntity u
                join u.offeredSkills os
                where lower(os) like ?1
                """, pattern).list();
        }

        StringBuilder jpql = new StringBuilder("""
            select distinct u
            from UserEntity u
            join u.offeredSkills os
            where """);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            if (i > 0) {
                jpql.append(" or ");
            }
            jpql.append(" lower(os) like ?").append(i + 1);
            params.add("%" + terms.get(i) + "%");
        }
        return find(jpql.toString(), params.toArray()).list();
    }
    public List<UserEntity> findAllUsers(){
        return findAll().list();
    }

    public List<UserEntity> findRandom10Users(){
        List<UserEntity> allUsers = findAll().list();

        if (allUsers.isEmpty()) {
            return List.of();
        }

        if (allUsers.size() <= 10) {
            return allUsers;
        }

        Collections.shuffle(allUsers, new Random());
        return allUsers.subList(0, 10);
    }


}
