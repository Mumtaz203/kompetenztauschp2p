package de.thws.kompetenz.chatting.adapter.in.rest.dto;

import java.util.UUID;

public class ShowConversationRequest {
    UUID conversationId;
    public ShowConversationRequest(UUID conversationId) {
        this.conversationId = conversationId;
    }
    public UUID getConversationId() {
        return conversationId;
    }
    public void setConversationId(UUID conversationId) {
        this.conversationId = conversationId;
    }
}
