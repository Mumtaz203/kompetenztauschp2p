package de.thws.kompetenz.chatting.adapter.in.rest.mapper;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.CreateMessageRequest;
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

    public Message toModel(CreateMessageRequest messageRequest) {
        Message message= new Message();
        message.setContent(messageRequest.getContent());
        message.setConversationId(messageRequest.getConversationId());
        message.setSenderId(messageRequest.getSenderId());
        message.setRecipientId(messageRequest.getRecipientId());
        return message;
    }
}
