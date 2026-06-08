package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.EmbeddingClientPort;
import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.application.service.recommendation.RecommendationScore;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendation;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendationScorer;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.matching.domain.model.SkillType;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiscoverUsersServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort;

    @Mock
    private SkillEmbeddingService skillEmbeddingService;

    @Mock
    private UserRecommendationScorer userRecommendationScorer;

    private DiscoverUsersService discoverUsersService;

    @BeforeEach
    void setUp() {
        discoverUsersService = new DiscoverUsersService(
                userRepositoryPort,
                skillEmbeddingRepositoryPort,
                skillEmbeddingService,
                userRecommendationScorer
        );
    }

    @Test
    void recommendUsers_returnsEmptyList_whenCurrentUserIdIsNull() {
        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(null);

        assertTrue(recommendations.isEmpty());
        verify(userRepositoryPort, never()).findUserById(any());
    }

    @Test
    void recommendUsers_returnsEmptyList_whenCurrentUserIsNotFound() {
        UUID currentUserId = UUID.randomUUID();
        when(userRepositoryPort.findUserById(currentUserId)).thenReturn(Optional.empty());

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUserId);

        assertTrue(recommendations.isEmpty());
        verify(userRepositoryPort, never()).findAllUsers();
    }

    @Test
    void recommendUsers_doesNotRecommendCurrentUserToThemselves() {
        User currentUser = user("current");
        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(currentUser));
        when(skillEmbeddingRepositoryPort.findByUserId(currentUser.getId())).thenReturn(List.of());

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertTrue(recommendations.isEmpty());
        verify(userRecommendationScorer, never()).score(any(), any(), any(), any());
    }

    @Test
    void recommendUsers_skipsNullUsersAndUsersWithoutId() {
        User currentUser = user("current");
        User withoutId = new User();
        User candidate = user("candidate");
        List<SkillEmbedding> currentEmbeddings = List.of(embedding(currentUser.getId(), "Backend", SkillType.WANTED));
        List<SkillEmbedding> candidateEmbeddings = List.of(embedding(candidate.getId(), "Java", SkillType.OFFERED));

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(listWithNullUser(withoutId, candidate));
        when(skillEmbeddingRepositoryPort.findByUserId(currentUser.getId())).thenReturn(currentEmbeddings);
        when(skillEmbeddingRepositoryPort.findByUserId(candidate.getId())).thenReturn(candidateEmbeddings);
        when(userRecommendationScorer.score(currentUser, candidate, currentEmbeddings, candidateEmbeddings))
                .thenReturn(score(60, 60, 0, 0, 1.0, "Java"));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(1, recommendations.size());
        assertEquals(candidate.getId(), recommendations.get(0).getUserId());
        verify(userRecommendationScorer).score(currentUser, candidate, currentEmbeddings, candidateEmbeddings);
        verify(skillEmbeddingRepositoryPort, never()).findByUserId(withoutId.getId());
    }

    @Test
    void recommendUsers_scoresCandidatesUsingUserRecommendationScorer() {
        User currentUser = user("current");
        User candidate = user("candidate");
        List<SkillEmbedding> currentEmbeddings = List.of(embedding(currentUser.getId(), "Backend", SkillType.WANTED));
        List<SkillEmbedding> candidateEmbeddings = List.of(embedding(candidate.getId(), "Java", SkillType.OFFERED));

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(candidate));
        when(skillEmbeddingRepositoryPort.findByUserId(currentUser.getId())).thenReturn(currentEmbeddings);
        when(skillEmbeddingRepositoryPort.findByUserId(candidate.getId())).thenReturn(candidateEmbeddings);
        when(userRecommendationScorer.score(currentUser, candidate, currentEmbeddings, candidateEmbeddings))
                .thenReturn(score(60, 60, 0, 0, 1.0, "Java"));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(1, recommendations.size());
        assertEquals("Your wanted skills match this user's offered skills.", recommendations.get(0).getMatchReason());
        verify(userRecommendationScorer).score(currentUser, candidate, currentEmbeddings, candidateEmbeddings);
    }

    @Test
    void recommendUsers_filtersOutZeroScoreCandidates() {
        User currentUser = user("current");
        User zeroScoreCandidate = user("zero");
        User positiveCandidate = user("positive");

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(zeroScoreCandidate, positiveCandidate));
        when(skillEmbeddingRepositoryPort.findByUserId(any())).thenReturn(List.of());
        when(userRecommendationScorer.score(eq(currentUser), eq(zeroScoreCandidate), any(), any()))
                .thenReturn(score(0, 0, 0, 0, 0.0));
        when(userRecommendationScorer.score(eq(currentUser), eq(positiveCandidate), any(), any()))
                .thenReturn(score(40, 0, 40, 0, 1.0, "Math"));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(1, recommendations.size());
        assertEquals(positiveCandidate.getId(), recommendations.get(0).getUserId());
        assertEquals("This user is interested in skills you offer.", recommendations.get(0).getMatchReason());
    }

    @Test
    void recommendUsers_sortsRecommendationsByScoreDescending() {
        User currentUser = user("current");
        User lowerScoreCandidate = user("lower");
        User higherScoreCandidate = user("higher");

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(lowerScoreCandidate, higherScoreCandidate));
        when(skillEmbeddingRepositoryPort.findByUserId(any())).thenReturn(List.of());
        when(userRecommendationScorer.score(eq(currentUser), eq(lowerScoreCandidate), any(), any()))
                .thenReturn(score(40, 0, 40, 0, 1.0));
        when(userRecommendationScorer.score(eq(currentUser), eq(higherScoreCandidate), any(), any()))
                .thenReturn(score(60, 60, 0, 0, 1.0));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(List.of(higherScoreCandidate.getId(), lowerScoreCandidate.getId()), recommendations.stream()
                .map(UserRecommendation::getUserId)
                .toList());
    }

    @Test
    void recommendUsers_usesBestSimilarityDescendingAsTieBreaker() {
        User currentUser = user("current");
        User weakerCandidate = user("weaker");
        User strongerCandidate = user("stronger");

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(weakerCandidate, strongerCandidate));
        when(skillEmbeddingRepositoryPort.findByUserId(any())).thenReturn(List.of());
        when(userRecommendationScorer.score(eq(currentUser), eq(weakerCandidate), any(), any()))
                .thenReturn(score(60, 60, 0, 0, 0.75));
        when(userRecommendationScorer.score(eq(currentUser), eq(strongerCandidate), any(), any()))
                .thenReturn(score(60, 60, 0, 0, 0.95));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(List.of(strongerCandidate.getId(), weakerCandidate.getId()), recommendations.stream()
                .map(UserRecommendation::getUserId)
                .toList());
    }

    @Test
    void recommendUsers_usesUsernameAscendingAsFinalTieBreakerWithNullsLast() {
        User currentUser = user("current");
        User nullNameCandidate = new User(UUID.randomUUID(), null, "null@test.com", "password");
        User alphaCandidate = user("alpha");
        User betaCandidate = user("Beta");

        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(nullNameCandidate, betaCandidate, alphaCandidate));
        when(skillEmbeddingRepositoryPort.findByUserId(any())).thenReturn(List.of());
        when(userRecommendationScorer.score(eq(currentUser), any(User.class), any(), any()))
                .thenReturn(score(60, 60, 0, 0, 1.0));

        List<UserRecommendation> recommendations = discoverUsersService.recommendUsers(currentUser.getId());

        assertEquals(List.of(alphaCandidate.getId(), betaCandidate.getId(), nullNameCandidate.getId()), recommendations.stream()
                .map(UserRecommendation::getUserId)
                .toList());
    }

    @Test
    void recommendUsers_ensuresCurrentUserEmbeddingsThroughSkillEmbeddingService() {
        User currentUser = user("current");
        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of());

        discoverUsersService.recommendUsers(currentUser.getId());

        verify(skillEmbeddingService).ensureOfferedSkillEmbeddings(currentUser);
        verify(skillEmbeddingService).ensureWantedSkillEmbeddings(currentUser);
    }

    @Test
    void recommendUsers_loadsEmbeddingsThroughSkillEmbeddingRepositoryPort() {
        User currentUser = user("current");
        User candidate = user("candidate");
        when(userRepositoryPort.findUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(userRepositoryPort.findAllUsers()).thenReturn(List.of(candidate));
        when(skillEmbeddingRepositoryPort.findByUserId(currentUser.getId())).thenReturn(List.of());
        when(skillEmbeddingRepositoryPort.findByUserId(candidate.getId())).thenReturn(List.of());
        when(userRecommendationScorer.score(eq(currentUser), eq(candidate), any(), any()))
                .thenReturn(score(0, 0, 0, 0, 0.0));

        discoverUsersService.recommendUsers(currentUser.getId());

        verify(skillEmbeddingRepositoryPort).findByUserId(currentUser.getId());
        verify(skillEmbeddingRepositoryPort).findByUserId(candidate.getId());
    }

    @Test
    void discoverUsersService_doesNotDependOnEmbeddingClientPortDirectly() {
        for (Field field : DiscoverUsersService.class.getDeclaredFields()) {
            assertFalse(
                    EmbeddingClientPort.class.equals(field.getType()),
                    "Discover service must use SkillEmbeddingService instead of calling Gemini directly"
            );
        }
    }

    private static User user(String username) {
        return new User(UUID.randomUUID(), username, username + "@test.com", "password");
    }

    private static SkillEmbedding embedding(UUID userId, String skillText, SkillType skillType) {
        return new SkillEmbedding(UUID.randomUUID(), userId, skillText, skillType, List.of(1.0, 0.0));
    }

    private static RecommendationScore score(
            int totalScore,
            int myWantedTheirOfferedScore,
            int theirWantedMyOfferedScore,
            int twoWayBonus,
            double bestSimilarity,
            String... matchedSkills
    ) {
        return new RecommendationScore(
                totalScore,
                myWantedTheirOfferedScore,
                theirWantedMyOfferedScore,
                twoWayBonus,
                bestSimilarity,
                List.of(matchedSkills)
        );
    }

    private static List<User> listWithNullUser(User... users) {
        ArrayList<User> result = new ArrayList<>();
        result.add(null);
        result.addAll(List.of(users));
        return result;
    }
}
