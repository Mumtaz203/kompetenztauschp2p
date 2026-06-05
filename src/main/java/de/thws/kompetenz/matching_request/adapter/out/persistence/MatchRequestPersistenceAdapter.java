package de.thws.kompetenz.matching_request.adapter.out.persistence;

import de.thws.kompetenz.matching_request.adapter.out.persistence.entity.MatchRequestEntity;
import de.thws.kompetenz.matching_request.adapter.out.persistence.mapper.MatchRequestPersistenceMapper;
import de.thws.kompetenz.matching_request.adapter.out.persistence.repository.MatchRequestPanacheRepository;
import de.thws.kompetenz.matching_request.application.port.out.MatchRequestRepositoryPortI;
import de.thws.kompetenz.matching_request.domain.MatchRequestModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
@Transactional
public class MatchRequestPersistenceAdapter implements MatchRequestRepositoryPortI {

    private final MatchRequestPanacheRepository repository;
    private final MatchRequestPersistenceMapper mapper;


    public MatchRequestPersistenceAdapter(MatchRequestPanacheRepository repository, MatchRequestPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override
    public MatchRequestModel save(MatchRequestModel model) {
        if(model.getId() == null) {
            MatchRequestEntity entity = mapper.toEntity(model);
            repository.persist(entity);
            return mapper.toModel(entity);
        }

        Optional<MatchRequestEntity> existingOpt = repository.findByIdOptional(model.getId());
        if(existingOpt.isEmpty()){//if we cant find any entity with the given id we create a new one
            MatchRequestEntity entity = mapper.toEntity(model);
            repository.persist(entity);
            return mapper.toModel(entity);
        }

        MatchRequestEntity entity = existingOpt.get();
        mapper.updateEntityFromDomain(model, entity);
        repository.persist(entity);
        return mapper.toModel(entity);

    }

    @Override
    public Optional<MatchRequestModel> findById(UUID requestId) {
       return  repository
               .findByIdOptional(requestId)
               .map(mapper::toModel);
    }

    @Override
    public Optional<MatchRequestModel> findPendingBetweenUsers(UUID senderId, UUID receiverId) {
        return repository.findPendingBetweenUsers(senderId, receiverId)
                .map(mapper::toModel);
    }

    @Override
    public Optional<MatchRequestModel> findPendingBetweenUsersBothDirections(UUID userAId, UUID userBId) {
        return repository.findPendingBetweenUsersBothDirections(userAId, userBId)
                .map(mapper::toModel);
    }

    @Override
    public List<MatchRequestModel> findIncomingPendingRequests(UUID receiverId) {
        return repository.findIncomingPendingRequests(receiverId)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public List<MatchRequestModel> findOutgoingPendingRequests(UUID senderId) {
        return repository.findOutgoingPendingRequests(senderId)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public List<MatchRequestModel> findAcceptedRequestsForUser(UUID userId) {
        return repository.findAcceptedReqestsForUser(userId)
                .stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public MatchRequestModel deleteById(UUID requestId) {
        if (requestId==null){
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        MatchRequestEntity existingEntity = repository.findByIdOptional(requestId).orElseThrow(()->new IllegalArgumentException("Request ID not found"));
        MatchRequestModel model = mapper.toModel(existingEntity);
        repository.delete(existingEntity);
        return model;
    }




}
