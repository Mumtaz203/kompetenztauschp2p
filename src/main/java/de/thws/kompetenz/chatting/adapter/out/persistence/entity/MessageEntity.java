package de.thws.kompetenz.chatting.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="messages")
public class MessageEntity {
    @Id
    @UuidGenerator
    UUID id;

    @Column(name="sent_at",nullable=false)
    LocalDateTime sentAt;

    @Column (name="sender_id",nullable=false)
    UUID senderId;
    @Column (name="recipient_id",nullable=false)
    UUID recipientId;
    @Column (name="content",nullable=false,columnDefinition = "TEXT")
    String content;
    @Column (name="is_read",nullable = false)
    boolean isRead=false;

    @ManyToOne(fetch = FetchType.LAZY,optional = false) //to insure low data amount ,is loaded when loading messages, we can load the conversation only when needed
    //lele optional bounds messages to conversations so messages cannot exist without conversations, this is a design choice, if we want to allow messages without conversations we can set optional to true and handle the case when conversation is null
    @JoinColumn(name="conversation_id",nullable = false)
    ConversationEntity conversationEntity;
    public MessageEntity() {
    }

    public MessageEntity(ConversationEntity conversationEntity, UUID senderId, UUID recipientId, String content) {
        this.conversationEntity = conversationEntity;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(UUID recipientId) {
        this.recipientId = recipientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public ConversationEntity getConversationEntity() {
        return conversationEntity;
    }

    public void setConversationEntity(ConversationEntity conversationEntity) {
        this.conversationEntity = conversationEntity;
    }
}
