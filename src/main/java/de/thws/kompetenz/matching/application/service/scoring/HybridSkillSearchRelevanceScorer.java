package de.thws.kompetenz.matching.application.service.scoring;

import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class HybridSkillSearchRelevanceScorer {

    private static final int EXACT_MATCH_SCORE = 10;
    private static final int PARTIAL_MATCH_SCORE = 3;

    public MatchRanking rankUser(User user, List<String> searchTerms, SemanticUserMatch semanticMatch) {
        if (user == null || searchTerms == null || searchTerms.isEmpty()) {
            return new MatchRanking(0, 0, 0, 0, 0, 0);
        }

        List<String> offeredSkills = user.getOfferedSkills();
        if (offeredSkills == null || offeredSkills.isEmpty()) {
            return new MatchRanking(0, 0, 0, 0, 0, 0);
        }

        int lexicalScore = 0;
        int matchedTermCount = 0;
        int exactMatchCount = 0;
        int partialMatchCount = 0;

        for (String term : searchTerms) {
            int termScore = scoreForTerm(term, offeredSkills);
            lexicalScore += termScore;
            if (termScore == EXACT_MATCH_SCORE) {
                matchedTermCount++;
                exactMatchCount++;
            } else if (termScore == PARTIAL_MATCH_SCORE) {
                matchedTermCount++;
                partialMatchCount++;
            }
        }

        int semanticScore = 0;
        int semanticMatchCount = 0;
        if (semanticMatch != null) {
            semanticScore = semanticMatch.semanticScore();
            semanticMatchCount = semanticMatch.semanticMatchCount();
        }

        int totalScore = lexicalScore + semanticScore;
        int totalMatchedTermCount = matchedTermCount + (semanticMatchCount > 0 ? 1 : 0);

        return new MatchRanking(
                totalScore,
                totalMatchedTermCount,
                exactMatchCount,
                semanticMatchCount,
                partialMatchCount,
                countOfferedSkills(offeredSkills)
        );
    }

    private int scoreForTerm(String term, List<String> offeredSkills) {
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

    private int countOfferedSkills(List<String> offeredSkills) {
        int count = 0;
        for (String skill : offeredSkills) {
            if (skill != null && !skill.isBlank()) {
                count++;
            }
        }
        return count;
    }
}
