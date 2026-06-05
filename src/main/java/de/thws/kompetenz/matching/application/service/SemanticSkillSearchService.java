package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.application.service.scoring.CosineSimilarityCalculator;
import de.thws.kompetenz.matching.application.service.scoring.SemanticUserMatch;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SemanticSkillSearchService {

    private static final double SIMILARITY_THRESHOLD = 0.65;

    private final EmbeddingClientPort embeddingClientPort;
    private final SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public SemanticSkillSearchService(
            EmbeddingClientPort embeddingClientPort,
            SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort,
            CosineSimilarityCalculator cosineSimilarityCalculator
    ) {
        this.embeddingClientPort = embeddingClientPort;
        this.skillEmbeddingRepositoryPort = skillEmbeddingRepositoryPort;
        this.cosineSimilarityCalculator = cosineSimilarityCalculator;
    }

    public Map<UUID, SemanticUserMatch> findSemanticMatches(List<String> searchTerms) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            return Map.of();
        }

        Map<UUID, SemanticUserMatch> semanticMatches = new LinkedHashMap<>();

        try {
            List<SkillEmbedding> allOfferedEmbeddings = skillEmbeddingRepositoryPort.findAllOfferedSkillEmbeddings();
            if (allOfferedEmbeddings == null || allOfferedEmbeddings.isEmpty()) {
                return Map.of();
            }

            for (String searchTerm : searchTerms) {
                if (searchTerm == null || searchTerm.isBlank()) {
                    continue;
                }

                List<Double> queryEmbedding = embeddingClientPort.createEmbedding(searchTerm.trim());
                if (queryEmbedding == null || queryEmbedding.isEmpty()) {
                    continue;
                }

                for (SkillEmbedding storedEmbedding : allOfferedEmbeddings) {
                    if (storedEmbedding == null || storedEmbedding.getEmbedding() == null) {
                        continue;
                    }

                    double similarity = cosineSimilarityCalculator.calculate(queryEmbedding, storedEmbedding.getEmbedding());
                    if (similarity < SIMILARITY_THRESHOLD) {
                        continue;
                    }

                    UUID userId = storedEmbedding.getUserId();
                    int semanticScore = (int) Math.round(similarity * 7.0);
                    semanticScore = Math.max(1, Math.min(7, semanticScore));

                    SemanticUserMatch existing = semanticMatches.get(userId);
                    if (existing == null) {
                        semanticMatches.put(userId, new SemanticUserMatch(
                                userId,
                                semanticScore,
                                1,
                                similarity
                        ));
                    } else {
                        if (similarity > existing.bestSimilarity()) {
                            semanticMatches.put(userId, new SemanticUserMatch(
                                    userId,
                                    semanticScore,
                                    existing.semanticMatchCount() + 1,
                                    similarity
                            ));
                        } else {
                            semanticMatches.put(userId, new SemanticUserMatch(
                                    userId,
                                    existing.semanticScore() + semanticScore,
                                    existing.semanticMatchCount() + 1,
                                    existing.bestSimilarity()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Map.of();
        }

        return semanticMatches;
    }
}
