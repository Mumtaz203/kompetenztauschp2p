package de.thws.kompetenz.user.adapter.out.persistence;

import de.thws.kompetenz.user.adapter.out.persistence.entity.UserEntity;
import de.thws.kompetenz.user.adapter.out.persistence.mapper.UserPersistenceMapper;
import de.thws.kompetenz.user.adapter.out.persistence.repository.UserPanacheRepository;
import jakarta.persistence.PersistenceException;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

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
        try {
            UserEntity entity = userPersistenceMapper.toEntity(user);
            userPanacheRepository.persist(entity);
            return userPersistenceMapper.toDomain(entity);
        } catch (PersistenceException e) {
            // optionally map DB constraint to domain exception later
            throw e;
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userPanacheRepository.findByEmail(email).map(userPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPanacheRepository.existsByEmail(email);
    }
}
