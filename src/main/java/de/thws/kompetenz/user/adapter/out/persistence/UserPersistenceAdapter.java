package de.thws.kompetenz.user.adapter.out.persistence;

import de.thws.kompetenz.user.adapter.out.persistence.entity.UserEntity;
import de.thws.kompetenz.user.adapter.out.persistence.mapper.UserPersistenceMapper;
import de.thws.kompetenz.user.adapter.out.persistence.repository.UserPanacheRepository;
import jakarta.persistence.PersistenceException;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class UserPersistenceAdapter implements UserRepositoryPort {

    // Uses EntityManager internally so we can use methods like persists etc.
    private final UserPanacheRepository userPanacheRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    public UserPersistenceAdapter(UserPanacheRepository userPanacheRepository, UserPersistenceMapper userPersistenceMapper) {
        this.userPanacheRepository = userPanacheRepository;
        this.userPersistenceMapper = userPersistenceMapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        // create
        if (user.getId() == null) {
            UserEntity entity = userPersistenceMapper.toEntity(user);
            userPanacheRepository.persist(entity);
            return userPersistenceMapper.toDomain(entity);
        }

        // update
        UserEntity existing = userPanacheRepository.findUserById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));

        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        existing.setPassword(user.getPassword());
        existing.setOfferedSkills(user.getOfferedSkills());
        existing.setWantedSkills(user.getWantedSkills());

        return userPersistenceMapper.toDomain(existing);

        //save method is now used for both create and update . Later it will be fixed
        //its necessery to control if id exists in order to update the user data

    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userPanacheRepository.findByEmail(email).map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPanacheRepository.existsByEmail(email);
    }


    @Override
    public List<User> searchBySkill(String skill) {
        return userPanacheRepository.findUsersBySkill(skill).stream()
                .map(userPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public List<User> findCandidatesByOfferedSkills(List<String> skills) {
        return userPanacheRepository.findUsersByOfferedSkillsContainingAny(skills).stream()
                .map(userPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findUserById(UUID userId) {
        return userPanacheRepository.findUserById(userId).map(userPersistenceMapper::toDomain);
    }

    @Override
    public List<User> findAllUsers() {
      return  userPanacheRepository.findAllUsers().stream()
              .map(ue->userPersistenceMapper.toDomain(ue))
              .toList();
    }
}
