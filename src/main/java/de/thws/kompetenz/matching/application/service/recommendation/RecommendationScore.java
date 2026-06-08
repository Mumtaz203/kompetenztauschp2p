package de.thws.kompetenz.matching.application.service.recommendation;

import java.util.List;

public record RecommendationScore(
        int totalScore,
        int myWantedTheirOfferedScore,
        int theirWantedMyOfferedScore,
        int twoWayBonus,
        double bestSimilarity,
        List<String> matchedSkills
) {
    public RecommendationScore {
        matchedSkills = matchedSkills == null ? List.of() : List.copyOf(matchedSkills);
    }
}
