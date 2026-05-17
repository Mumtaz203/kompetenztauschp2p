package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.user.domain.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class SkillSearchRelevanceScorer {

    static final int EXACT_MATCH_SCORE = 10;
    static final int PARTIAL_MATCH_SCORE = 3;

    private SkillSearchRelevanceScorer() {
    }

    static int calculateScore(User user, List<String> searchTerms) {
        if (user == null || searchTerms == null || searchTerms.isEmpty()) {
            return 0;
        }
        List<String> offeredSkills = user.getOfferedSkills();
        if (offeredSkills == null || offeredSkills.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        for (String term : searchTerms) {
            totalScore += scoreForTerm(term, offeredSkills);
        }
        return totalScore;
    }

    static int scoreForTerm(String term, List<String> offeredSkills) {
        if (term == null || term.isBlank()) {
            return 0;
        }
        String normalizedTerm = term.trim().toLowerCase(Locale.ROOT);

        boolean hasPartialMatch = false;
        for (String offeredSkill : offeredSkills) {
            if (offeredSkill == null || offeredSkill.isBlank()) {
                continue;
            }
            String normalizedSkill = offeredSkill.trim().toLowerCase(Locale.ROOT);
            if (normalizedSkill.equals(normalizedTerm)) {
                return EXACT_MATCH_SCORE;
            }
            if (normalizedSkill.contains(normalizedTerm)) {
                hasPartialMatch = true;
            }
        }
        return hasPartialMatch ? PARTIAL_MATCH_SCORE : 0;
    }
}
