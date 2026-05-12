package de.thws.kompetenz.chatting.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Conversation {

    UUID id;
    UUID user1Id;
    UUID user2Id;
    LocalDateTime createdAt;
    LocalDateTime lastMessageAt;

    public Conversation() {
    }

    public Conversation(UUID id, UUID user1Id, UUID user2Id, LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.id = id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
    }

    public Conversation(UUID user1Id, UUID user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    public void touchLastMessageAt() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public boolean hasParticipant(UUID userId) {
        return userId != null && (userId.equals(user1Id) || userId.equals(user2Id));
    }

    public UUID getOtherParticipant(UUID userId) {
        if (userId == null) return null;
        if (userId.equals(user1Id)) return user2Id;
        if (userId.equals(user2Id)) return user1Id;
        return null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(UUID user1Id) {
        this.user1Id = user1Id;
    }

    public UUID getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(UUID user2Id) {
        this.user2Id = user2Id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }
}
