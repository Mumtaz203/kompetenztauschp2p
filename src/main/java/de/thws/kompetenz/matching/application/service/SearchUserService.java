package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.application.port.in.SearchUserUseCase;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SearchUserService implements SearchUserUseCase {
    private static final Comparator<RankedUser> BY_RELEVANCE_AND_TIE_BREAKERS = Comparator
            .comparingInt(RankedUser::score).reversed()
            .thenComparingInt(RankedUser::matchedTermCount).reversed()
            .thenComparingInt(RankedUser::exactMatchCount).reversed()
            .thenComparingInt(RankedUser::partialMatchCount).reversed()
            .thenComparingInt(RankedUser::offeredSkillsCount).reversed()
            .thenComparing(ranked -> ranked.user().getUsername(),
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));

    private final UserRepositoryPort userRepositoryPort;

    public SearchUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
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
        for (User candidate : userRepositoryPort.findCandidatesByOfferedSkills(searchTerms)) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            RankedUser ranked = toRankedUser(candidate, searchTerms);
            if (ranked.score() == 0) {
                continue;
            }
            uniqueResults.merge(candidate.getId(), ranked, SearchUserService::keepBetterRankedUser);
        }

        return uniqueResults.values().stream()
                .sorted(BY_RELEVANCE_AND_TIE_BREAKERS)
                .map(RankedUser::user)
                .toList();
    }

    private static RankedUser toRankedUser(User user, List<String> searchTerms) {
        SkillSearchRelevanceScorer.MatchRanking ranking = SkillSearchRelevanceScorer.rankUser(user, searchTerms);
        return new RankedUser(
                user,
                ranking.score(),
                ranking.matchedTermCount(),
                ranking.exactMatchCount(),
                ranking.partialMatchCount(),
                ranking.offeredSkillsCount()
        );
    }

    private static RankedUser keepBetterRankedUser(RankedUser existing, RankedUser incoming) {
        return BY_RELEVANCE_AND_TIE_BREAKERS.compare(incoming, existing) < 0 ? incoming : existing;
    }

    private record RankedUser(
            User user,
            int score,
            int matchedTermCount,
            int exactMatchCount,
            int partialMatchCount,
            int offeredSkillsCount
    ) {
    }
}
