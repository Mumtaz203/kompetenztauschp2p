package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SkillEmbeddingService {

    private final EmbeddingClientPort embeddingClientPort;
    private final SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort;

    public SkillEmbeddingService(
            EmbeddingClientPort embeddingClientPort,
            SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort
    ) {
        this.embeddingClientPort = embeddingClientPort;
        this.skillEmbeddingRepositoryPort = skillEmbeddingRepositoryPort;
    }

    public List<SkillEmbedding> ensureOfferedSkillEmbeddings(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }

        List<String> normalizedSkills = normalizeSkillTexts(user.getOfferedSkills());
        if (normalizedSkills.isEmpty()) {
            return List.of();
        }

        List<SkillEmbedding> skillEmbeddings = new ArrayList<>();
        for (String skillText : normalizedSkills) {
            SkillEmbedding skillEmbedding = ensureSkillEmbedding(user.getId(), skillText, SkillType.OFFERED);
            if (skillEmbedding != null) {
                skillEmbeddings.add(skillEmbedding);
            }
        }
        return skillEmbeddings;
    }

    public SkillEmbedding ensureSkillEmbedding(UUID userId, String skillText, SkillType skillType) {
        if (userId == null || skillType == null) {
            return null;
        }

        String normalizedSkillText = normalizeSkillText(skillText);
        if (normalizedSkillText == null) {
            return null;
        }

        Optional<SkillEmbedding> existing = skillEmbeddingRepositoryPort
                .findByUserIdAndSkillTextAndSkillType(userId, normalizedSkillText, skillType);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<Double> embedding = embeddingClientPort.createEmbedding(normalizedSkillText);
        SkillEmbedding skillEmbedding = new SkillEmbedding();
        skillEmbedding.setUserId(userId);
        skillEmbedding.setSkillText(normalizedSkillText);
        skillEmbedding.setSkillType(skillType);
        skillEmbedding.setEmbedding(embedding);

        return skillEmbeddingRepositoryPort.save(skillEmbedding);
    }

    private List<String> normalizeSkillTexts(List<String> offeredSkills) {
        if (offeredSkills == null || offeredSkills.isEmpty()) {
            return List.of();
        }

        Map<String, String> distinctSkills = new LinkedHashMap<>();
        for (String skill : offeredSkills) {
            String normalized = normalizeSkillText(skill);
            if (normalized == null) {
                continue;
            }
            String dedupeKey = normalized.toLowerCase(Locale.ROOT);
            distinctSkills.putIfAbsent(dedupeKey, normalized);
        }
        return new ArrayList<>(distinctSkills.values());
    }

    private String normalizeSkillText(String skillText) {
        if (skillText == null) {
            return null;
        }
        String normalized = skillText.trim();
        return normalized.isBlank() ? null : normalized;
    }
}
