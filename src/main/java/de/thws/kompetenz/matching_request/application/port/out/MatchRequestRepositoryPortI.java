package de.thws.kompetenz.matching_request.application.port.out;

import de.thws.kompetenz.matching_request.domain.MatchRequestModel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRequestRepositoryPortI {
    MatchRequestModel save(MatchRequestModel model);

    Optional<MatchRequestModel> findById(UUID requestId);

    Optional<MatchRequestModel> findPendingBetweenUsers(UUID senderId, UUID receiverId);

    Optional<MatchRequestModel> findPendingBetweenUsersBothDirections(UUID userAId, UUID userBId);

    List<MatchRequestModel> findIncomingPendingRequests(UUID receiverId);

    List<MatchRequestModel> findOutgoingPendingRequests(UUID senderId);

    List<MatchRequestModel> findAcceptedRequestsForUser(UUID userId);

    MatchRequestModel deleteById(UUID requestId);




}
