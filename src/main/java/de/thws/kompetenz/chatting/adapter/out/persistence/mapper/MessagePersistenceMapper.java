package de.thws.kompetenz.chatting.adapter.out.persistence.mapper;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.MessageEntity;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessagePersistenceMapper {
    public Message toDomain(MessageEntity entity) {

        return new  Message(entity.getId(),
               entity.getConversationEntity().getId(),
               entity.getSenderId(),
               entity.getRecipientId(),
               entity.getContent(),
               entity.getSentAt(),
               entity.isRead());

      //we dont have a toEntity method because for MessageEntity we need the ConversationEntity which will be find with its id from Repository
    }
}
