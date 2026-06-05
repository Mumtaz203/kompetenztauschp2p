package de.thws.kompetenz.matching.domain.model;

import java.util.List;
import java.util.UUID;

public class SkillEmbedding {

    private UUID id;
    private UUID userId;
    private String skillText;
    private SkillType skillType;
    private List<Double> embedding;

    public SkillEmbedding() {
    }

    public SkillEmbedding(UUID id, UUID userId, String skillText, SkillType skillType, List<Double> embedding) {
        this.id = id;
        this.userId = userId;
        this.skillText = skillText;
        this.skillType = skillType;
        this.embedding = embedding;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSkillText() {
        return skillText;
    }

    public void setSkillText(String skillText) {
        this.skillText = skillText;
    }

    public SkillType getSkillType() {
        return skillType;
    }

    public void setSkillType(SkillType skillType) {
        this.skillType = skillType;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
