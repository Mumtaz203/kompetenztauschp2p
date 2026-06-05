package de.thws.kompetenz.matching.application.port.out;

import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillEmbeddingRepositoryPort {

    SkillEmbedding save(SkillEmbedding skillEmbedding);

    Optional<SkillEmbedding> findByUserIdAndSkillTextAndSkillType(
            UUID userId,
            String skillText,
            SkillType skillType
    );

    List<SkillEmbedding> findByUserId(UUID userId);

    List<SkillEmbedding> findBySkillType(SkillType skillType);

    List<SkillEmbedding> findAllOfferedSkillEmbeddings();

    void deleteByUserIdAndSkillType(UUID userId, SkillType skillType);
}
