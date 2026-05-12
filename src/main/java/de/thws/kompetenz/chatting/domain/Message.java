package de.thws.kompetenz.chatting.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private UUID id;
    private UUID conversationId;
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private LocalDateTime sentAt;
    private boolean read;


    public Message() {
    }

    public Message(UUID id, UUID conversationId, UUID senderId, UUID recipientId, String content, LocalDateTime sentAt, boolean read) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.sentAt = sentAt;
        this.read = read;
    }

    public Message(UUID conversationId, UUID senderId, UUID recipientId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.read = false;
    }

    public void markAsRead() {
        this.read = true;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
