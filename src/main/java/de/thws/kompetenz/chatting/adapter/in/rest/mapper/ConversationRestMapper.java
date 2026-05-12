package de.thws.kompetenz.chatting.adapter.in.rest.mapper;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.ConversationResponse;
import de.thws.kompetenz.chatting.domain.Conversation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConversationRestMapper {
    public ConversationResponse toResponse(Conversation conversation) {
        if(conversation == null) {
            return null;
        }
      ConversationResponse response = new ConversationResponse();
        response.setId(conversation.getId());
        response.setUser1Id(conversation.getUser1Id());
        response.setUser2Id(conversation.getUser2Id());
        response.setCreatedAt(conversation.getCreatedAt());
        response.setLastMessageAt(conversation.getLastMessageAt());
        return response;
    }
}
