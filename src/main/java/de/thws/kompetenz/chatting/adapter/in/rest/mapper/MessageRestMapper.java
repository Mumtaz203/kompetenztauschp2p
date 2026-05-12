package de.thws.kompetenz.chatting.adapter.in.rest.mapper;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageRestMapper {

    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setConversationId(message.getConversationId());
        response.setSenderId(message.getSenderId());
        response.setRecipientId(message.getRecipientId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setRead(message.isRead());
        return response;
    }
}
