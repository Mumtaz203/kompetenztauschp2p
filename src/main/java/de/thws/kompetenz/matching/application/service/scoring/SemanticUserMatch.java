package de.thws.kompetenz.matching.application.service.scoring;

import java.util.UUID;

public record SemanticUserMatch(
        UUID userId,
        int semanticScore,
        int semanticMatchCount,
        double bestSimilarity
) {
}
