package de.thws.kompetenz.chatting.adapter.out.persistence.mapper;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.ConversationEntity;
import de.thws.kompetenz.chatting.domain.Conversation;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConversationPersistenceMapper {

    public ConversationEntity toEntity(Conversation conversation) {
        if(conversation == null) {
            return null;
        }
       ConversationEntity conversationEntity = new ConversationEntity();
        conversationEntity.setId(conversation.getId());
        conversationEntity.setCreatedAt(conversation.getCreatedAt());
        conversationEntity.setLastMessageAt(conversation.getLastMessageAt());
        conversationEntity.setUser1Id(conversation.getUser1Id());
        conversationEntity.setUser2Id(conversation.getUser2Id());
        return conversationEntity;
    }

    public Conversation toDomain(ConversationEntity conversationEntity) {
        if(conversationEntity == null) {
            return null;
        }

            return new Conversation(
                    conversationEntity.getId(),
                    conversationEntity.getUser1Id(),
                    conversationEntity.getUser2Id(),
                    conversationEntity.getCreatedAt(),
                    conversationEntity.getLastMessageAt());
    }
}
