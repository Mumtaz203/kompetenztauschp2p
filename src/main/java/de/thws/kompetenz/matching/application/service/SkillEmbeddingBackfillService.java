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
        int usersWithOfferedSkills = 0;
        int embeddingsEnsured = 0;

        for (User user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }

            usersChecked++;
            if (user.getOfferedSkills() == null || user.getOfferedSkills().isEmpty()) {
                continue;
            }

            usersWithOfferedSkills++;
            List<SkillEmbedding> ensured = skillEmbeddingService.ensureOfferedSkillEmbeddings(user);
            if (ensured != null) {
                embeddingsEnsured += ensured.size();
            }
        }

        return new BackfillResult(usersChecked, usersWithOfferedSkills, embeddingsEnsured);
    }
}
