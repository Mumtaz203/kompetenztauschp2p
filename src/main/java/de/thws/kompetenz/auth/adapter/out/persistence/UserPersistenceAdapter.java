package de.thws.kompetenz.auth.adapter.out.persistence;

import de.thws.kompetenz.auth.adapter.out.persistence.entity.UserEntity;
import de.thws.kompetenz.auth.adapter.out.persistence.mapper.UserPersistenceMapper;
import de.thws.kompetenz.auth.adapter.out.persistence.repository.UserPanacheRepository;
import de.thws.kompetenz.auth.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.auth.domain.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserPanacheRepository userPanacheRepository;
    private final UserPersistenceMapper userPersistenceMapper;

    public UserPersistenceAdapter(UserPanacheRepository userPanacheRepository, UserPersistenceMapper userPersistenceMapper) {
        this.userPanacheRepository = userPanacheRepository;
        this.userPersistenceMapper = userPersistenceMapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = userPersistenceMapper.toEntity(user);
        userPanacheRepository.persist(entity);
        return userPersistenceMapper.toDomain(entity);
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
