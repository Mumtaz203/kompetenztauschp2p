package de.thws.kompetenz.matching.adapter.out.persistence.repository;

import de.thws.kompetenz.matching.adapter.out.persistence.entity.SkillEmbeddingEntity;
import de.thws.kompetenz.matching.domain.model.SkillType;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SkillEmbeddingJpaRepository implements PanacheRepositoryBase<SkillEmbeddingEntity, UUID> {

    public Optional<SkillEmbeddingEntity> findByUserIdAndSkillTextAndSkillType(UUID userId, String skillText, SkillType skillType) {
        if (userId == null || skillText == null || skillType == null) {
            return Optional.empty();
        }
        return find("userId = ?1 and skillText = ?2 and skillType = ?3", userId, skillText, skillType)
                .firstResultOptional();
    }

    public List<SkillEmbeddingEntity> findByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return find("userId = ?1", userId).list();
    }

    public List<SkillEmbeddingEntity> findBySkillType(SkillType skillType) {
        if (skillType == null) {
            return List.of();
        }
        return find("skillType = ?1", skillType).list();
    }

    public List<SkillEmbeddingEntity> findAllOfferedSkillEmbeddings() {
        return find("skillType = ?1", SkillType.OFFERED).list();
    }

    public void deleteByUserIdAndSkillType(UUID userId, SkillType skillType) {
        if (userId == null || skillType == null) {
            return;
        }
        delete("userId = ?1 and skillType = ?2", userId, skillType);
    }
}
