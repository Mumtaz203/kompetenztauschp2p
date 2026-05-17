package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillSearchRelevanceScorerTest {

    @Test
    void scoreForTerm_exactMatchScoresHigherThanPartial() {
        int exact = SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("sql"));
        int partial = SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("mysql"));

        assertEquals(SkillSearchRelevanceScorer.EXACT_MATCH_SCORE, exact);
        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE, partial);
        assertTrue(exact > partial);
    }

    @Test
    void scoreForTerm_partialMatchForMysqlAndPostgresql() {
        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE,
                SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("mysql")));
        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE,
                SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("postgresql")));
    }

    @Test
    void scoreForTerm_noMatchReturnsZero() {
        assertEquals(0, SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("java")));
    }

    @Test
    void calculateScore_sumsBestMatchPerTerm() {
        User user = user("sql_java_user", "sql", "java");
        int score = SkillSearchRelevanceScorer.calculateScore(user, List.of("sql", "java"));

        assertEquals(2 * SkillSearchRelevanceScorer.EXACT_MATCH_SCORE, score);
    }

    @Test
    void calculateScore_partialPlusExactForMultipleTerms() {
        User user = user("mysql_java_user", "mysql", "java");
        int score = SkillSearchRelevanceScorer.calculateScore(user, List.of("sql", "java"));

        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE + SkillSearchRelevanceScorer.EXACT_MATCH_SCORE,
                score);
    }

    @Test
    void scoreForTerm_multiplePartialMatchesCountOncePerTerm() {
        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE,
                SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("mysql", "postgresql")));
    }

    @Test
    void scoreForTerm_isCaseInsensitive() {
        assertEquals(SkillSearchRelevanceScorer.EXACT_MATCH_SCORE,
                SkillSearchRelevanceScorer.scoreForTerm("SQL", List.of("sql")));
        assertEquals(SkillSearchRelevanceScorer.PARTIAL_MATCH_SCORE,
                SkillSearchRelevanceScorer.scoreForTerm("sql", List.of("MySQL")));
    }

    private static User user(String username, String... skills) {
        User user = new User(UUID.randomUUID(), username, username + "@test.com", "secret");
        user.setOfferedSkills(List.of(skills));
        return user;
    }
}
