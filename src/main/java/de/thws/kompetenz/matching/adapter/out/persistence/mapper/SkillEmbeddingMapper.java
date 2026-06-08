package de.thws.kompetenz.matching.adapter.out.persistence.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.thws.kompetenz.matching.adapter.out.persistence.entity.SkillEmbeddingEntity;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SkillEmbeddingMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<Double>> DOUBLE_LIST_TYPE = new TypeReference<>() {
    };

    public SkillEmbeddingEntity toEntity(SkillEmbedding domain) {
        if (domain == null) {
            return null;
        }

        SkillEmbeddingEntity entity = new SkillEmbeddingEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setSkillText(domain.getSkillText());
        entity.setSkillType(domain.getSkillType());
        entity.setEmbeddingJson(toJson(domain.getEmbedding()));
        return entity;
    }

    public SkillEmbedding toDomain(SkillEmbeddingEntity entity) {
        if (entity == null) {
            return null;
        }

        SkillEmbedding domain = new SkillEmbedding(
                entity.getId(),
                entity.getUserId(),
                entity.getSkillText(),
                entity.getSkillType(),
                fromJson(entity.getEmbeddingJson())
        );
        return domain;
    }

    private String toJson(List<Double> embedding) {
        if (embedding == null) {
            return "[]";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(new ArrayList<>(embedding));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize embedding list", e);
        }
    }

    private List<Double> fromJson(String embeddingJson) {
        if (embeddingJson == null || embeddingJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(embeddingJson, DOUBLE_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize embedding json", e);
        }
    }
}
