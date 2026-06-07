package de.thws.kompetenz.matching.application.service.recommendation;

import de.thws.kompetenz.matching.application.service.scoring.CosineSimilarityCalculator;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class UserRecommendationScorer {

    private static final double SEMANTIC_THRESHOLD = 0.65;
    private static final int MY_WANTED_THEIR_OFFERED_MAX_POINTS = 60;
    private static final int THEIR_WANTED_MY_OFFERED_MAX_POINTS = 40;
    private static final int TWO_WAY_BONUS_POINTS = 20;

    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    public UserRecommendationScorer(CosineSimilarityCalculator cosineSimilarityCalculator) {
        this.cosineSimilarityCalculator = cosineSimilarityCalculator;
    }

    public RecommendationScore score(
            User currentUser,
            User candidateUser,
            List<SkillEmbedding> currentUserEmbeddings,
            List<SkillEmbedding> candidateUserEmbeddings
    ) {
        if (currentUser == null || candidateUser == null) {
            return zeroScore();
        }
        if (currentUser.getId() != null && currentUser.getId().equals(candidateUser.getId())) {
            return zeroScore();
        }
        if (currentUserEmbeddings == null || currentUserEmbeddings.isEmpty()
                || candidateUserEmbeddings == null || candidateUserEmbeddings.isEmpty()) {
            return zeroScore();
        }

        DirectionScore myWantedTheirOffered = scoreDirection(
                currentUserEmbeddings,
                SkillType.WANTED,
                candidateUserEmbeddings,
                SkillType.OFFERED,
                MY_WANTED_THEIR_OFFERED_MAX_POINTS
        );
        DirectionScore theirWantedMyOffered = scoreDirection(
                candidateUserEmbeddings,
                SkillType.WANTED,
                currentUserEmbeddings,
                SkillType.OFFERED,
                THEIR_WANTED_MY_OFFERED_MAX_POINTS
        );

        int twoWayBonus = myWantedTheirOffered.score() > 0 && theirWantedMyOffered.score() > 0
                ? TWO_WAY_BONUS_POINTS
                : 0;
        int totalScore = myWantedTheirOffered.score() + theirWantedMyOffered.score() + twoWayBonus;
        double bestSimilarity = Math.max(myWantedTheirOffered.bestSimilarity(), theirWantedMyOffered.bestSimilarity());

        Set<String> matchedSkills = new LinkedHashSet<>();
        matchedSkills.addAll(myWantedTheirOffered.matchedSkills());
        matchedSkills.addAll(theirWantedMyOffered.matchedSkills());

        return new RecommendationScore(
                totalScore,
                myWantedTheirOffered.score(),
                theirWantedMyOffered.score(),
                twoWayBonus,
                bestSimilarity,
                new ArrayList<>(matchedSkills)
        );
    }

    public String matchReason(RecommendationScore score) {
        if (score == null || score.totalScore() == 0) {
            return "This user is a semantic skill match.";
        }
        if (score.twoWayBonus() > 0) {
            return "Two-way skill match found.";
        }
        if (score.myWantedTheirOfferedScore() > 0) {
            return "Your wanted skills match this user's offered skills.";
        }
        return "This user is a semantic skill match.";
    }

    private DirectionScore scoreDirection(
            List<SkillEmbedding> sourceEmbeddings,
            SkillType sourceSkillType,
            List<SkillEmbedding> targetEmbeddings,
            SkillType targetSkillType,
            int maxPoints
    ) {
        double bestSimilarity = 0.0;
        Set<String> matchedSkills = new LinkedHashSet<>();

        for (SkillEmbedding sourceEmbedding : sourceEmbeddings) {
            if (!isValidEmbedding(sourceEmbedding, sourceSkillType)) {
                continue;
            }
            for (SkillEmbedding targetEmbedding : targetEmbeddings) {
                if (!isValidEmbedding(targetEmbedding, targetSkillType)) {
                    continue;
                }

                double similarity = cosineSimilarityCalculator.calculate(
                        sourceEmbedding.getEmbedding(),
                        targetEmbedding.getEmbedding()
                );
                if (similarity >= SEMANTIC_THRESHOLD) {
                    bestSimilarity = Math.max(bestSimilarity, similarity);
                    addMatchedSkill(matchedSkills, targetEmbedding.getSkillText());
                }
            }
        }

        int score = bestSimilarity >= SEMANTIC_THRESHOLD
                ? (int) Math.round(bestSimilarity * maxPoints)
                : 0;
        return new DirectionScore(score, bestSimilarity, new ArrayList<>(matchedSkills));
    }

    private boolean isValidEmbedding(SkillEmbedding skillEmbedding, SkillType skillType) {
        return skillEmbedding != null
                && skillEmbedding.getSkillType() == skillType
                && skillEmbedding.getEmbedding() != null
                && !skillEmbedding.getEmbedding().isEmpty();
    }

    private void addMatchedSkill(Set<String> matchedSkills, String skillText) {
        if (skillText == null || skillText.isBlank()) {
            return;
        }
        matchedSkills.add(skillText.trim());
    }

    private RecommendationScore zeroScore() {
        return new RecommendationScore(0, 0, 0, 0, 0.0, List.of());
    }

    private record DirectionScore(
            int score,
            double bestSimilarity,
            List<String> matchedSkills
    ) {
    }
}
