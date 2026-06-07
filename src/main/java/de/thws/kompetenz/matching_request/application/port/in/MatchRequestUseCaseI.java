package de.thws.kompetenz.matching_request.application.port.in;

import de.thws.kompetenz.matching_request.domain.MatchRequestModel;

import java.util.*;

public interface MatchRequestUseCaseI {
    MatchRequestModel sendRequest(UUID senderId, UUID receiverId);
    MatchRequestModel acceptRequest(UUID requestId, UUID actingUserId);
    MatchRequestModel rejectRequest(UUID requestId, UUID actingUserId);
    List<MatchRequestModel> getIncomingPendingRequests(UUID receiverId);
    List<MatchRequestModel> getOutgoingPendingRequests(UUID senderId);
    List<MatchRequestModel> getAcceptedMatches(UUID userId);
    MatchRequestModel adminUpdate(UUID requestId, MatchRequestModel updated);
    MatchRequestModel adminDelete(UUID requestId);
}
