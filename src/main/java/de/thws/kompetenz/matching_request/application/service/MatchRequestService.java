package de.thws.kompetenz.matching_request.application.service;

import de.thws.kompetenz.matching_request.adapter.out.persistence.MatchRequestPersistenceAdapter;
import de.thws.kompetenz.matching_request.application.port.in.MatchRequestUseCaseI;
import de.thws.kompetenz.matching_request.application.port.out.MatchRequestRepositoryPortI;
import de.thws.kompetenz.matching_request.domain.MatchRequestModel;
import de.thws.kompetenz.matching_request.domain.MatchRequestStatus;
import de.thws.kompetenz.session.application.port.in.ICreateSessionUseCase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class MatchRequestService implements MatchRequestUseCaseI {
    private final MatchRequestRepositoryPortI adapter;
    private final ICreateSessionUseCase createSessionUseCase;

    public MatchRequestService( MatchRequestRepositoryPortI matchRequestRepositoryPort, ICreateSessionUseCase createSessionUseCase) {
        this.adapter= matchRequestRepositoryPort;
        this.createSessionUseCase = createSessionUseCase;

    }

    @Override
    public MatchRequestModel sendRequest(UUID senderId, UUID receiverId) {
        validateIds(senderId, receiverId);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and Receiver IDs cannot be the same");
        }
        boolean pendingExists = adapter.findPendingBetweenUsersBothDirections(senderId, receiverId).isPresent();
        //we check first if there is already a pending request between the two users in either direction
        if(pendingExists) {
            throw new IllegalStateException("a pending match request already exists between these users");
        }

        MatchRequestModel model = new MatchRequestModel();
        model.setSenderId(senderId);
        model.setReceiverId(receiverId);
        model.setStatus(MatchRequestStatus.PENDING);

        return adapter.save(model);

    }

    @Override
    @Transactional
    public MatchRequestModel acceptRequest(UUID requestId, UUID actingUserId) {
        if (requestId == null || actingUserId == null) {
            throw new IllegalArgumentException("requestId and actingUserId cannot be null");
        }

        MatchRequestModel model = adapter.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found: " + requestId));

        if (!actingUserId.equals(model.getReceiverId())) {
            throw new IllegalArgumentException("Only the receiver of the match request can accept it");
        }

        if (!model.getStatus().equals(MatchRequestStatus.PENDING)) {
            throw new IllegalStateException("Only pending match requests can be accepted");
        }

        model.setStatus(MatchRequestStatus.ACCEPTED);

        MatchRequestModel accepted = adapter.save(model);

        createSessionUseCase.createSession(
                accepted.getId(),
                accepted.getSenderId(),
                accepted.getReceiverId()
        );

        return accepted;
    }

    @Override
    public MatchRequestModel rejectRequest(UUID requestId, UUID actingUserId) {
        if (requestId == null || actingUserId == null) {
            throw new IllegalArgumentException("requestId and actingUserId cannot be null");
        }
        MatchRequestModel model = adapter.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found: " + requestId));

        if (!actingUserId.equals(model.getReceiverId())) {
            throw new IllegalArgumentException("Only the receiver of the match request can accept it");
        }
        if (!model.getStatus().equals(MatchRequestStatus.PENDING)) {
            throw new IllegalStateException("only pending match request can be accepted");
        }
        return adapter.deleteById(requestId);

    }

    @Override
    public List<MatchRequestModel> getIncomingPendingRequests(UUID receiverId) {
        if(receiverId == null) {
            throw new IllegalArgumentException("receiverId cannot be null");
        }
        return adapter.findIncomingPendingRequests(receiverId);
    }

    @Override
    public List<MatchRequestModel> getOutgoingPendingRequests(UUID senderId) {
        if(senderId == null) {
            throw new IllegalArgumentException("senderId cannot be null");
        }
        return adapter.findOutgoingPendingRequests(senderId);
    }

    @Override
    public List<MatchRequestModel> getAcceptedMatches(UUID userId) {
      if (userId == null) {
          throw new IllegalArgumentException("userId cannot be null");
      }
      return adapter.findAcceptedRequestsForUser(userId);
    }

    @Override
    public MatchRequestModel adminUpdate(UUID requestId, MatchRequestModel updated) {
        if (requestId == null || updated == null) {
            throw new IllegalArgumentException("requestId and updated model are required");
        }

        MatchRequestModel existing = adapter.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Match request not found: " + requestId));

        if (updated.getSenderId() != null) existing.setSenderId(updated.getSenderId());
        if (updated.getReceiverId() != null) existing.setReceiverId(updated.getReceiverId());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());

        return adapter.save(existing);
    }

    @Override
    public MatchRequestModel adminDelete(UUID requestId) {
        //i just thought that we admins are going to update or delete some requests ,maybe later
        //we can implements somthing like if the request is rejected from receiver side and the sender want to delete it from his side or something like that
        //then we can let
            if (requestId == null) {
                throw new IllegalArgumentException("requestId is required");
            }
         return   adapter.deleteById(requestId);

    }


    private void validateIds(UUID senderId, UUID receiverId) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("senderId and receiverId are required");
        }
    }
}
