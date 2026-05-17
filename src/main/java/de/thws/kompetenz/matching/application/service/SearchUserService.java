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

        Map<UUID, ScoredUser> uniqueResults = new LinkedHashMap<>();
        for (User candidate : userRepositoryPort.findCandidatesByOfferedSkills(searchTerms)) {
            if (candidate == null || candidate.getId() == null) {
                continue;
            }
            int score = SkillSearchRelevanceScorer.calculateScore(candidate, searchTerms);
            if (score == 0) {
                continue;
            }
            uniqueResults.merge(
                    candidate.getId(),
                    new ScoredUser(candidate, score),
                    SearchUserService::keepHigherScoredUser
            );
        }

        return uniqueResults.values().stream()
                .sorted(Comparator
                        .comparingInt(ScoredUser::score).reversed()
                        .thenComparing(scored -> scored.user().getUsername(),
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                        .thenComparing(scored -> scored.user().getId(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ScoredUser::user)
                .toList();
    }

    private static ScoredUser keepHigherScoredUser(ScoredUser existing, ScoredUser incoming) {
        return incoming.score() > existing.score() ? incoming : existing;
    }

    private record ScoredUser(User user, int score) {
    }
}
