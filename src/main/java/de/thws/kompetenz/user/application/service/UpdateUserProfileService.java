package de.thws.kompetenz.user.application.service;

import de.thws.kompetenz.user.application.port.in.UpdateUserProfileUseCase;


import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.matching.application.service.SkillEmbeddingService;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class UpdateUserProfileService implements UpdateUserProfileUseCase {
    private final UserRepositoryPort userRepositoryPort;
    private final SkillEmbeddingService skillEmbeddingService;

    public UpdateUserProfileService(UserRepositoryPort userRepositoryPort, SkillEmbeddingService skillEmbeddingService) {
        this.userRepositoryPort = userRepositoryPort;
        this.skillEmbeddingService = skillEmbeddingService;
    }
    @Override
    public User updateSkills(UUID userId, List<String> offeredSkills, List<String> wantedSkills) {
     User user= userRepositoryPort.findUserById(userId).orElseThrow(()->
             new IllegalArgumentException("User not found "+userId) );
    user.setOfferedSkills(normalizeSkills(offeredSkills));
    user.setWantedSkills(normalizeSkills(wantedSkills));
    User saved = userRepositoryPort.save(user);
    // Trigger embedding generation after user is persisted only when offered skills present
    if (saved.getOfferedSkills() != null && !saved.getOfferedSkills().isEmpty()) {
        skillEmbeddingService.ensureOfferedSkillEmbeddings(saved);
    }
    return saved;


    }

    @Override
    public User updateName(UUID userId, String name) {
        User user= userRepositoryPort.findUserById(userId)
                .orElseThrow(()->new IllegalArgumentException("User not found "+userId) );
        if(name==null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        user.setUsername(name.trim());
      return  userRepositoryPort.save(user);

    }

    @Override
    public User updateUser(UUID userId, User incomingUser) {
        User existingUser= userRepositoryPort.findUserById(userId).orElseThrow(()->new IllegalArgumentException("User not found "+userId) );
        if (incomingUser == null
        ) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if(incomingUser.getUsername()!=null&&!incomingUser.getUsername().isBlank()){
            existingUser.setUsername(incomingUser.getUsername().trim());
        }
        existingUser.setOfferedSkills(normalizeSkills(incomingUser.getOfferedSkills()));
        existingUser.setWantedSkills(normalizeSkills(incomingUser.getWantedSkills()));
           User saved = userRepositoryPort.save(existingUser);
           // Trigger embedding generation after user is persisted only when offered skills present
           if (saved.getOfferedSkills() != null && !saved.getOfferedSkills().isEmpty()) {
               skillEmbeddingService.ensureOfferedSkillEmbeddings(saved);
           }
           return saved;



    }

    public List<String> normalizeSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        return skills.stream().map(s->s.trim()).filter(s->!s.isBlank()).map(s->s.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.collectingAndThen(
                java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                List::copyOf));
    }
}
