package de.thws.kompetenz.matching.application.service.recommendation;

import de.thws.kompetenz.matching.application.service.scoring.CosineSimilarityCalculator;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserRecommendationScorerTest {

    private UserRecommendationScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new UserRecommendationScorer(new CosineSimilarityCalculator());
    }

    @Test
    void score_returnsForwardScore_whenCurrentWantedMatchesCandidateOffered() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0))),
                List.of(embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(1.0, 0.0)))
        );

        assertEquals(60, score.totalScore());
        assertEquals(60, score.myWantedTheirOfferedScore());
        assertEquals(0, score.theirWantedMyOfferedScore());
        assertEquals(0, score.twoWayBonus());
        assertEquals(1.0, score.bestSimilarity(), 0.0001);
    }

    @Test
    void score_returnsReverseScore_whenCandidateWantedMatchesCurrentOffered() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(embedding(currentUser.getId(), "Math", SkillType.OFFERED, List.of(0.0, 1.0))),
                List.of(embedding(candidateUser.getId(), "Math tutoring", SkillType.WANTED, List.of(0.0, 1.0)))
        );

        assertEquals(40, score.totalScore());
        assertEquals(0, score.myWantedTheirOfferedScore());
        assertEquals(40, score.theirWantedMyOfferedScore());
        assertEquals(0, score.twoWayBonus());
        assertEquals(1.0, score.bestSimilarity(), 0.0001);
    }

    @Test
    void score_addsTwoWayBonus_whenBothDirectionsMatch() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(
                        embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0)),
                        embedding(currentUser.getId(), "Math", SkillType.OFFERED, List.of(0.0, 1.0))
                ),
                List.of(
                        embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(1.0, 0.0)),
                        embedding(candidateUser.getId(), "Math tutoring", SkillType.WANTED, List.of(0.0, 1.0))
                )
        );

        assertEquals(120, score.totalScore());
        assertEquals(60, score.myWantedTheirOfferedScore());
        assertEquals(40, score.theirWantedMyOfferedScore());
        assertEquals(20, score.twoWayBonus());
    }

    @Test
    void score_returnsZero_whenUsersHaveSameId() {
        UUID userId = UUID.randomUUID();
        User currentUser = new User(userId, "current", "current@test.com", "password");
        User candidateUser = new User(userId, "candidate", "candidate@test.com", "password");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(embedding(userId, "Backend", SkillType.WANTED, List.of(1.0, 0.0))),
                List.of(embedding(userId, "Java", SkillType.OFFERED, List.of(1.0, 0.0)))
        );

        assertZeroScore(score);
    }

    @Test
    void score_returnsZero_whenUsersAreNull() {
        User user = user("user");
        List<SkillEmbedding> embeddings = List.of(embedding(user.getId(), "Java", SkillType.OFFERED, List.of(1.0)));

        assertZeroScore(scorer.score(null, user, embeddings, embeddings));
        assertZeroScore(scorer.score(user, null, embeddings, embeddings));
    }

    @Test
    void score_returnsZero_whenEmbeddingsAreMissing() {
        User currentUser = user("current");
        User candidateUser = user("candidate");
        List<SkillEmbedding> embeddings = List.of(embedding(currentUser.getId(), "Java", SkillType.OFFERED, List.of(1.0)));

        assertZeroScore(scorer.score(currentUser, candidateUser, null, embeddings));
        assertZeroScore(scorer.score(currentUser, candidateUser, embeddings, null));
        assertZeroScore(scorer.score(currentUser, candidateUser, List.of(), embeddings));
        assertZeroScore(scorer.score(currentUser, candidateUser, embeddings, List.of()));
    }

    @Test
    void score_ignoresWeakSimilarityBelowThreshold() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0))),
                List.of(embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(0.0, 1.0)))
        );

        assertZeroScore(score);
    }

    @Test
    void score_collectsMatchedSkillsWithoutDuplicates() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(
                        embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0)),
                        embedding(currentUser.getId(), "API", SkillType.WANTED, List.of(1.0, 0.0)),
                        embedding(currentUser.getId(), "Math", SkillType.OFFERED, List.of(0.0, 1.0))
                ),
                List.of(
                        embedding(candidateUser.getId(), " Java ", SkillType.OFFERED, List.of(1.0, 0.0)),
                        embedding(candidateUser.getId(), "Math tutoring", SkillType.WANTED, List.of(0.0, 1.0))
                )
        );

        assertIterableEquals(List.of("Java", "Math"), score.matchedSkills());
    }

    @Test
    void score_calculatesBestSimilarityCorrectly() {
        User currentUser = user("current");
        User candidateUser = user("candidate");

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                List.of(embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0))),
                List.of(embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(0.8, 0.6)))
        );

        assertEquals(48, score.totalScore());
        assertEquals(0.8, score.bestSimilarity(), 0.0001);
    }

    @Test
    void score_isDeterministic() {
        User currentUser = user("current");
        User candidateUser = user("candidate");
        List<SkillEmbedding> currentEmbeddings = List.of(
                embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0)),
                embedding(currentUser.getId(), "Math", SkillType.OFFERED, List.of(0.0, 1.0))
        );
        List<SkillEmbedding> candidateEmbeddings = List.of(
                embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(1.0, 0.0)),
                embedding(candidateUser.getId(), "Math tutoring", SkillType.WANTED, List.of(0.0, 1.0))
        );

        RecommendationScore firstScore = scorer.score(currentUser, candidateUser, currentEmbeddings, candidateEmbeddings);
        RecommendationScore secondScore = scorer.score(currentUser, candidateUser, currentEmbeddings, candidateEmbeddings);

        assertEquals(firstScore, secondScore);
    }

    @Test
    void score_ignoresNullAndEmptyVectorEmbeddingsSafely() {
        User currentUser = user("current");
        User candidateUser = user("candidate");
        java.util.ArrayList<SkillEmbedding> currentEmbeddings = new java.util.ArrayList<>();
        currentEmbeddings.add(null);
        currentEmbeddings.add(embedding(currentUser.getId(), "Broken", SkillType.WANTED, List.of()));
        currentEmbeddings.add(embedding(currentUser.getId(), "Backend", SkillType.WANTED, List.of(1.0, 0.0)));

        RecommendationScore score = scorer.score(
                currentUser,
                candidateUser,
                currentEmbeddings,
                List.of(embedding(candidateUser.getId(), "Java", SkillType.OFFERED, List.of(1.0, 0.0)))
        );

        assertEquals(60, score.totalScore());
        assertTrue(score.matchedSkills().contains("Java"));
    }

    private static User user(String username) {
        return new User(UUID.randomUUID(), username, username + "@test.com", "password");
    }

    private static SkillEmbedding embedding(UUID userId, String skillText, SkillType skillType, List<Double> vector) {
        return new SkillEmbedding(UUID.randomUUID(), userId, skillText, skillType, vector);
    }

    private static void assertZeroScore(RecommendationScore score) {
        assertEquals(0, score.totalScore());
        assertEquals(0, score.myWantedTheirOfferedScore());
        assertEquals(0, score.theirWantedMyOfferedScore());
        assertEquals(0, score.twoWayBonus());
        assertEquals(0.0, score.bestSimilarity(), 0.0001);
        assertTrue(score.matchedSkills().isEmpty());
    }
}
