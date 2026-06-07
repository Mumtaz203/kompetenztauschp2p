package de.thws.kompetenz.matching_request.adapter.in.rest.dto;

import de.thws.kompetenz.matching_request.domain.MatchRequestStatus;

import java.time.Instant;
import java.util.UUID;

public class MatchRequestResponseDTO {
    public UUID id;
    public UUID senderId;
    public UUID receiverId;
    public MatchRequestStatus status;
    public Instant createdAt;
    public Instant updatedAt;

    public MatchRequestResponseDTO() {
    }

    public MatchRequestResponseDTO(UUID id, UUID senderId, UUID receiverId, MatchRequestStatus status, Instant createdAt, Instant updatedAt) {
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
}