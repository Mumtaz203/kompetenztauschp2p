package de.thws.kompetenz.user.application.service;

import de.thws.kompetenz.auth.application.port.out.IPasswordHasherPort;
import de.thws.kompetenz.user.application.port.in.UserUseCaseI;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Locale;
import java.util.UUID;
@ApplicationScoped
@Transactional
public class UserService implements UserUseCaseI {

    private final UserRepositoryPort userRepository;
    private final IPasswordHasherPort passwordHasher;
    public UserService(UserRepositoryPort userRepository, IPasswordHasherPort passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }
    @Override
    public User deleteUserById(UUID userId) {
        return userRepository.deleteUserById(userId);
    }

    @Override
    public User updateUser(UUID userId, User user) {
        User existingUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setId(userId);
        // i did ask this part to ai if its okey and its said its okey because he said exactly like i thought that we update(also create)
        // the user from adapter with save method thats why its not necessery to implement any other method to the adapter (like updateUser) instead we do that in service

        String normalizedUsername = user.getUsername().trim();

        String normalizedEmail = user.getEmail().trim().toLowerCase(Locale.ROOT);
        String passwordHash = passwordHasher.hash(user.getPassword());


        user.setUsername(normalizedUsername);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordHash);
        user.setOfferedSkills(user.getOfferedSkills() == null ? existingUser.getOfferedSkills() : user.getOfferedSkills());
        user.setWantedSkills(user.getWantedSkills() == null ? existingUser.getWantedSkills() : user.getWantedSkills());
        user.setProfileImageUrl(user.getProfileImageUrl() == null ? existingUser.getProfileImageUrl() : normalizeNullableText(user.getProfileImageUrl()));
        user.setUniversity(user.getUniversity() == null ? existingUser.getUniversity() : normalizeNullableText(user.getUniversity()));
        user.setPrivateReportCount(existingUser.getPrivateReportCount());
        user.setInternallyFlagged(existingUser.isInternallyFlagged());

        return userRepository.save(user);
    }

    @Override
    public User updateInternalFlag(UUID userId, boolean internallyFlagged) {
        User existingUser = userRepository.findUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        existingUser.setInternallyFlagged(internallyFlagged);

        return userRepository.save(existingUser);
    }

    private String normalizeNullableText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
