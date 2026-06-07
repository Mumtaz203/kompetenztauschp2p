package de.thws.kompetenz.matching_request.adapter.out.persistence.repository;

import de.thws.kompetenz.matching_request.adapter.out.persistence.entity.MatchRequestEntity;
import de.thws.kompetenz.matching_request.domain.MatchRequestStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class MatchRequestPanacheRepository implements PanacheRepository<MatchRequestEntity> {
public Optional<MatchRequestEntity> findPendingBetweenUsers(UUID senderId, UUID receiverId) {
    return find("senderId = ?1 and receiverId = ?2 and status = ?3", senderId, receiverId, MatchRequestStatus.PENDING).firstResultOptional();
}


    public Optional<MatchRequestEntity> findPendingBetweenUsersBothDirections(UUID userAId,UUID userBId) {
    return find("(senderId = ?1 and receiverId = ?2 or senderId = ?2 and receiverId = ?1) and status = ?3" ,
            userAId, userBId, MatchRequestStatus.PENDING).firstResultOptional();
    }

    public List<MatchRequestEntity> findIncomingPendingRequests(UUID receiverId){
    return list("receiverId = ?1 and status = ?2" , receiverId, MatchRequestStatus.PENDING);

    }
    public List<MatchRequestEntity> findOutgoingPendingRequests(UUID senderId){
    return list("senderId = ?1 and status = ?2" ,senderId , MatchRequestStatus.PENDING);
    }
    public List<MatchRequestEntity> findAcceptedReqestsForUser (UUID userId){
    return list("senderId = ?1 or receiverId = ?1 and status = ?2" , userId, MatchRequestStatus.ACCEPTED);
    }
    public Optional<MatchRequestEntity> findByIdOptional(UUID id) {
        return find("id", id).firstResultOptional();
    }
}
