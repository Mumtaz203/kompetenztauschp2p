package de.thws.kompetenz.matching.application.service.scoring;

public record MatchRanking(
        int score,
        int matchedTermCount,
        int exactMatchCount,
        int semanticMatchCount,
        int partialMatchCount,
        int offeredSkillsCount
) {
}
