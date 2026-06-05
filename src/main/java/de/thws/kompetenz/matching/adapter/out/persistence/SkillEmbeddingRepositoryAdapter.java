package de.thws.kompetenz.matching.adapter.out.persistence;

import de.thws.kompetenz.matching.adapter.out.persistence.entity.SkillEmbeddingEntity;
import de.thws.kompetenz.matching.adapter.out.persistence.mapper.SkillEmbeddingMapper;
import de.thws.kompetenz.matching.adapter.out.persistence.repository.SkillEmbeddingJpaRepository;
import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SkillEmbeddingRepositoryAdapter implements SkillEmbeddingRepositoryPort {

    private final SkillEmbeddingJpaRepository skillEmbeddingJpaRepository;
    private final SkillEmbeddingMapper skillEmbeddingMapper;

    public SkillEmbeddingRepositoryAdapter(
            SkillEmbeddingJpaRepository skillEmbeddingJpaRepository,
            SkillEmbeddingMapper skillEmbeddingMapper
    ) {
        this.skillEmbeddingJpaRepository = skillEmbeddingJpaRepository;
        this.skillEmbeddingMapper = skillEmbeddingMapper;
    }

    @Override
    @Transactional
    public SkillEmbedding save(SkillEmbedding skillEmbedding) {
        if (skillEmbedding == null) {
            throw new IllegalArgumentException("SkillEmbedding cannot be null");
        }

        String normalizedSkillText = normalizeSkillText(skillEmbedding.getSkillText());
        if (normalizedSkillText == null) {
            throw new IllegalArgumentException("Skill text cannot be null or blank");
        }

        SkillEmbeddingEntity entity = skillEmbeddingMapper.toEntity(skillEmbedding);
        entity.setSkillText(normalizedSkillText);
        entity.setUpdatedAt(LocalDateTime.now());

        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
            skillEmbeddingJpaRepository.persist(entity);
            return skillEmbeddingMapper.toDomain(entity);
        }

        Optional<SkillEmbeddingEntity> existingEntity = skillEmbeddingJpaRepository.findByIdOptional(entity.getId());
        if (existingEntity.isEmpty()) {
            entity.setCreatedAt(LocalDateTime.now());
            skillEmbeddingJpaRepository.persist(entity);
            return skillEmbeddingMapper.toDomain(entity);
        }

        SkillEmbeddingEntity persistedEntity = existingEntity.get();
        persistedEntity.setUserId(entity.getUserId());
        persistedEntity.setSkillText(entity.getSkillText());
        persistedEntity.setSkillType(entity.getSkillType());
        persistedEntity.setEmbeddingJson(entity.getEmbeddingJson());
        persistedEntity.setUpdatedAt(entity.getUpdatedAt());

        return skillEmbeddingMapper.toDomain(persistedEntity);
    }

    @Override
    public Optional<SkillEmbedding> findByUserIdAndSkillTextAndSkillType(UUID userId, String skillText, SkillType skillType) {
        if (userId == null || skillType == null) {
            return Optional.empty();
        }
        String normalizedSkillText = normalizeSkillText(skillText);
        if (normalizedSkillText == null) {
            return Optional.empty();
        }
        return skillEmbeddingJpaRepository
                .findByUserIdAndSkillTextAndSkillType(userId, normalizedSkillText, skillType)
                .map(skillEmbeddingMapper::toDomain);
    }

    @Override
    public List<SkillEmbedding> findByUserId(UUID userId) {
        return skillEmbeddingJpaRepository.findByUserId(userId).stream()
                .map(skillEmbeddingMapper::toDomain)
                .toList();
    }

    @Override
    public List<SkillEmbedding> findBySkillType(SkillType skillType) {
        return skillEmbeddingJpaRepository.findBySkillType(skillType).stream()
                .map(skillEmbeddingMapper::toDomain)
                .toList();
    }

    @Override
    public List<SkillEmbedding> findAllOfferedSkillEmbeddings() {
        return skillEmbeddingJpaRepository.findAllOfferedSkillEmbeddings().stream()
                .map(skillEmbeddingMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndSkillType(UUID userId, SkillType skillType) {
        if (userId == null || skillType == null) {
            return;
        }
        skillEmbeddingJpaRepository.deleteByUserIdAndSkillType(userId, skillType);
    }

    private String normalizeSkillText(String skillText) {
        if (skillText == null) {
            return null;
        }
        String normalized = skillText.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
