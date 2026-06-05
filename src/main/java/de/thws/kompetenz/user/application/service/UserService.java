package de.thws.kompetenz.user.application.service;

import de.thws.kompetenz.user.application.port.in.UserUseCaseI;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;
@ApplicationScoped
@Transactional
public class UserService implements UserUseCaseI {

    private final UserRepositoryPort userRepository;
    public UserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public User deleteUserById(UUID userId) {
        return userRepository.deleteUserById(userId);
    }

    @Override
    public User updateUser(UUID userId, User user) {
        user.setId(userId); // i did ask this part to ai if its okey and its said its okey because he said exactly like i thought that we update(also create)
        // the user from adapter with save method thats why its not necessery to implement any other method to the adapter (like updateUser) instead we do that in service
        return userRepository.save(user);
    }
}
