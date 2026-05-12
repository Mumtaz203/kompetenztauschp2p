package de.thws.kompetenz.chatting.adapter.in.rest.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShowConversationResponse {
    private UUID conversationId;
    private UUID user1Id;
    private UUID user2Id;
    private String user1Name;
    private String user2Name;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private List<MessageResponse> messages;

    public UUID getConversationId() { return conversationId; }
    public void setConversationId(UUID conversationId) { this.conversationId = conversationId; }

    public UUID getUser1Id() { return user1Id; }
    public void setUser1Id(UUID user1Id) { this.user1Id = user1Id; }

    public UUID getUser2Id() { return user2Id; }
    public void setUser2Id(UUID user2Id) { this.user2Id = user2Id; }

    public String getUser1Name() { return user1Name; }
    public void setUser1Name(String user1Name) { this.user1Name = user1Name; }

    public String getUser2Name() { return user2Name; }
    public void setUser2Name(String user2Name) { this.user2Name = user2Name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public List<MessageResponse> getMessages() { return messages; }
    public void setMessages(List<MessageResponse> messages) { this.messages = messages; }
}
