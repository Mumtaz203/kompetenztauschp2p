package de.thws.kompetenz.matching.application.service;

import de.thws.kompetenz.matching.domain.model.SkillEmbedding;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class SkillEmbeddingBackfillService {

    private final UserRepositoryPort userRepositoryPort;
    private final SkillEmbeddingService skillEmbeddingService;

    public SkillEmbeddingBackfillService(
            UserRepositoryPort userRepositoryPort,
            SkillEmbeddingService skillEmbeddingService
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.skillEmbeddingService = skillEmbeddingService;
    }

    public BackfillResult generateMissingOfferedSkillEmbeddingsForAllUsers() {
        List<User> users = userRepositoryPort.findAllUsers();
        if (users == null || users.isEmpty()) {
            return new BackfillResult(0, 0, 0);
        }

        int usersChecked = 0;
        int usersWithSkills = 0;
        int embeddingsEnsured = 0;

        for (User user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }

            usersChecked++;
            boolean hasOfferedSkills = user.getOfferedSkills() != null && !user.getOfferedSkills().isEmpty();
            boolean hasWantedSkills = user.getWantedSkills() != null && !user.getWantedSkills().isEmpty();
            if (!hasOfferedSkills && !hasWantedSkills) {
                continue;
            }

            usersWithSkills++;
            if (hasOfferedSkills) {
                List<SkillEmbedding> offeredEnsured = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);
                if (offeredEnsured != null) {
                    embeddingsEnsured += offeredEnsured.size();
                }
            }
            if (hasWantedSkills) {
                List<SkillEmbedding> wantedEnsured = skillEmbeddingService.ensureWantedSkillEmbeddings(user);
                if (wantedEnsured != null) {
                    embeddingsEnsured += wantedEnsured.size();
                }
            }
        }

        return new BackfillResult(usersChecked, usersWithSkills, embeddingsEnsured);
    }
}
