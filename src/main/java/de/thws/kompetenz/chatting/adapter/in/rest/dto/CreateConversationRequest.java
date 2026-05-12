package de.thws.kompetenz.chatting.adapter.in.rest.dto;

import java.util.UUID;

public class CreateConversationRequest {
    private UUID currentUserId;
    private UUID otherUserId;

    public UUID getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(UUID currentUserId) {
        this.currentUserId = currentUserId;
    }

    public UUID getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(UUID otherUserId) {
        this.otherUserId = otherUserId;
    }
}