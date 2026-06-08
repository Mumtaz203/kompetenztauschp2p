package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import de.thws.kompetenz.matching.application.service.scoring.HybridSkillSearchRelevanceScorer;
import de.thws.kompetenz.matching.application.service.scoring.MatchRanking;
import de.thws.kompetenz.matching.application.service.scoring.SemanticUserMatch;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class SearchUserService implements SearchUserUseCase {

    private static final Comparator<RankedUser> BY_RELEVANCE_AND_TIE_BREAKERS = Comparator
            .comparingInt(RankedUser::score)
            .thenComparingInt(RankedUser::matchedTermCount)
            .thenComparingInt(RankedUser::exactMatchCount)
            .thenComparingInt(RankedUser::semanticMatchCount)
            .thenComparingInt(RankedUser::partialMatchCount)
            .thenComparingInt(RankedUser::offeredSkillsCount)
            .thenComparing(ranked -> ranked.user().getUsername(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
            .reversed();

    private final UserRepositoryPort userRepositoryPort;
    private final SemanticSkillSearchService semanticSkillSearchService;
    private final HybridSkillSearchRelevanceScorer hybridSkillSearchRelevanceScorer;

    public SearchUserService(
            UserRepositoryPort userRepositoryPort,
            SemanticSkillSearchService semanticSkillSearchService,
            HybridSkillSearchRelevanceScorer hybridSkillSearchRelevanceScorer
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.semanticSkillSearchService = semanticSkillSearchService;
        this.hybridSkillSearchRelevanceScorer = hybridSkillSearchRelevanceScorer;
    }

    @Override
    public List<User> searchBySkill(String skill) {
        if (skill == null || skill.isBlank()) {
            return List.of();
        }
        return searchBySkills(List.of(skill.trim().toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<User> searchBySkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }

        List<String> searchTerms = skills.stream()
                .filter(term -> term != null && !term.isBlank())
                .map(term -> term.trim().toLowerCase(Locale.ROOT))
                .toList();
        if (searchTerms.isEmpty()) {
            return List.of();
        }

        Map<UUID, RankedUser> uniqueResults = new LinkedHashMap<>();

        List<User> lexicalCandidates = userRepositoryPort.findCandidatesByOfferedSkills(searchTerms);
        for (User candidate : lexicalCandidates) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            RankedUser ranked = toRankedUser(candidate, searchTerms, null);
            if (ranked.score() > 0) {
                uniqueResults.put(candidate.getId(), ranked);
            }
        }

        Map<UUID, SemanticUserMatch> semanticMatches = semanticSkillSearchService.findSemanticMatches(searchTerms);
        List<User> allUsers = null;
        Map<UUID, User> userMap = new HashMap<>();

        for (UUID userId : semanticMatches.keySet()) {
            if (uniqueResults.containsKey(userId)) {
                RankedUser existing = uniqueResults.get(userId);
                SemanticUserMatch semantic = semanticMatches.get(userId);
                RankedUser updated = toRankedUser(existing.user(), searchTerms, semantic);
                uniqueResults.put(userId, updated);
            } else {
                if (allUsers == null) {
                    allUsers = userRepositoryPort.findAllUsers();
                    for (User user : allUsers) {
                        if (user != null && user.getId() != null) {
                            userMap.put(user.getId(), user);
                        }
                    }
                }

                User semanticUser = userMap.get(userId);
                if (semanticUser != null) {
                    SemanticUserMatch semantic = semanticMatches.get(userId);
                    RankedUser ranked = toRankedUser(semanticUser, searchTerms, semantic);
                    if (ranked.score() > 0) {
                        uniqueResults.put(userId, ranked);
                    }
                }
            }
        }

        return uniqueResults.values().stream()
                .sorted(BY_RELEVANCE_AND_TIE_BREAKERS)
                .map(RankedUser::user)
                .toList();
    }

    private RankedUser toRankedUser(User user, List<String> searchTerms, SemanticUserMatch semanticMatch) {
        MatchRanking ranking = hybridSkillSearchRelevanceScorer.rankUser(user, searchTerms, semanticMatch);
        return new RankedUser(
                user,
                ranking.score(),
                ranking.matchedTermCount(),
                ranking.exactMatchCount(),
                ranking.semanticMatchCount(),
                ranking.partialMatchCount(),
                ranking.offeredSkillsCount()
        );
    }

    private record RankedUser(
            User user,
            int score,
            int matchedTermCount,
            int exactMatchCount,
            int semanticMatchCount,
            int partialMatchCount,
            int offeredSkillsCount
    ) {
    }
}


