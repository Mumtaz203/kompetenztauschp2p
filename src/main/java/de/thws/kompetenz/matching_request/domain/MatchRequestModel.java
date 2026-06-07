package de.thws.kompetenz.matching_request.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MatchRequestModel {

    private UUID id;
    private UUID senderId;
    private UUID receiverId;
    private MatchRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public MatchRequestModel() {
    }

    public MatchRequestModel(
            UUID id,
            UUID senderId,
            UUID receiverId,
            MatchRequestStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public MatchRequestStatus getStatus() {
        return status;
    }

    public void setStatus(MatchRequestStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPending() {
        return MatchRequestStatus.PENDING.equals(this.status);
    }

    public void accept() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be accepted");
        }
        this.status = MatchRequestStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    public void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        this.status = MatchRequestStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void validateForCreate() {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("Sender and receiver IDs are required");
        }
        if (Objects.equals(senderId, receiverId)) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same user");
        }
        if (status == null) {
            status = MatchRequestStatus.PENDING;
        }
    }
}