package de.thws.kompetenz.chatting.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user1_id", "user2_id"})//this constraint ensures that there can be only one conversation between two users,
        // it prevents duplicate conversations between the same users,
        // it also allows us to easily find a conversation between two users by querying with their ids
})


public class ConversationEntity {
    @Id
    @UuidGenerator
    UUID id;

    @Column(name="user1_id",nullable=false,columnDefinition = "UUID")
    UUID user1Id;

    @Column(name="user2_id",nullable=false,columnDefinition ="UUID")
    UUID user2Id;

    @Column (name ="created_at",nullable=false)
    LocalDateTime createdAt;
    @Column (name ="last_message_at",nullable=false)
    LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "conversationEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    //cascade type ensures if conversation i mean parent is deleted so are childeren messages will be deleted lülü
    @OrderBy("sentAt ASC")
    List<MessageEntity> messages = new ArrayList<>();


    public ConversationEntity(){

    }
    public ConversationEntity(UUID user1Id, UUID user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }
    public ConversationEntity(UUID user1Id, UUID user2Id, LocalDateTime createdAt, LocalDateTime lastMessageAt) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.createdAt = createdAt;
        this.lastMessageAt = lastMessageAt;
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

    public List<MessageEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageEntity> messages) {
        this.messages = messages;
    }
}
