package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.out.SkillEmbeddingRepositoryPort;
import de.thws.kompetenz.matching.application.service.recommendation.RecommendationScore;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendation;
import de.thws.kompetenz.matching.application.service.recommendation.UserRecommendationScorer;
import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class DiscoverUsersService {

    private final UserRepositoryPort userRepositoryPort;
    private final SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort;
    private final SkillEmbeddingService skillEmbeddingService;
    private final UserRecommendationScorer userRecommendationScorer;

    public DiscoverUsersService(
            UserRepositoryPort userRepositoryPort,
            SkillEmbeddingRepositoryPort skillEmbeddingRepositoryPort,
            SkillEmbeddingService skillEmbeddingService,
            UserRecommendationScorer userRecommendationScorer
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.skillEmbeddingRepositoryPort = skillEmbeddingRepositoryPort;
        this.skillEmbeddingService = skillEmbeddingService;
        this.userRecommendationScorer = userRecommendationScorer;
    }

    public List<UserRecommendation> recommendUsers(UUID currentUserId) {
        if (currentUserId == null) {
            return List.of();
        }

        return userRepositoryPort.findUserById(currentUserId)
                .map(this::recommendUsersForCurrentUser)
                .orElse(List.of());
    }

    private List<UserRecommendation> recommendUsersForCurrentUser(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            return List.of();
        }

        skillEmbeddingService.ensureOfferedSkillEmbeddings(currentUser);
        skillEmbeddingService.ensureWantedSkillEmbeddings(currentUser);

        List<User> users = userRepositoryPort.findAllUsers();
        if (users == null || users.isEmpty()) {
            return List.of();
        }

        List<SkillEmbedding> currentUserEmbeddings = safeList(
                skillEmbeddingRepositoryPort.findByUserId(currentUser.getId())
        );

        return users.stream()
                .filter(candidate -> isRecommendationCandidate(currentUser, candidate))
                .map(candidate -> scoreCandidate(currentUser, currentUserEmbeddings, candidate))
                .filter(recommendation -> recommendation.getScore() > 0)
                .sorted(recommendationComparator())
                .toList();
    }

    private UserRecommendation scoreCandidate(
            User currentUser,
            List<SkillEmbedding> currentUserEmbeddings,
            User candidate
    ) {
        // TODO: optimize candidate embedding loading with repository queries, pgvector, or prepared backfill data.
        List<SkillEmbedding> candidateEmbeddings = safeList(
                skillEmbeddingRepositoryPort.findByUserId(candidate.getId())
        );
        RecommendationScore score = userRecommendationScorer.score(
                currentUser,
                candidate,
                currentUserEmbeddings,
                candidateEmbeddings
        );

        return new UserRecommendation(
                candidate.getId(),
                candidate.getUsername(),
                score.totalScore(),
                score.bestSimilarity(),
                score.matchedSkills(),
                matchReason(score)
        );
    }

    private boolean isRecommendationCandidate(User currentUser, User candidate) {
        return candidate != null
                && candidate.getId() != null
                && !candidate.getId().equals(currentUser.getId());
    }

    private List<SkillEmbedding> safeList(List<SkillEmbedding> embeddings) {
        return embeddings == null ? List.of() : embeddings;
    }

    private String matchReason(RecommendationScore score) {
        if (score.twoWayBonus() > 0) {
            return "Two-way skill match found.";
        }
        if (score.myWantedTheirOfferedScore() > 0) {
            return "Your wanted skills match this user's offered skills.";
        }
        if (score.theirWantedMyOfferedScore() > 0) {
            return "This user is interested in skills you offer.";
        }
        return "Semantic skill match found.";
    }

    private Comparator<UserRecommendation> recommendationComparator() {
        return Comparator
                .comparingInt(UserRecommendation::getScore).reversed()
                .thenComparing(Comparator.comparingDouble(UserRecommendation::getBestSimilarity).reversed())
                .thenComparing(
                        recommendation -> normalizeUsername(recommendation.getUsername()),
                        Comparator.nullsLast(String::compareTo)
                );
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }
        return username.toLowerCase(Locale.ROOT);
    }
}
