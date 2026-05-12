package de.thws.kompetenz.chatting.adapter.out.persistence.repository;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.MessageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class MessagePanacheRepository implements PanacheRepositoryBase<MessageEntity,UUID> {

    public Optional<MessageEntity> findMessageById(UUID messageId) {
        return find("id", messageId).firstResultOptional();
    }

    public List<MessageEntity> findByConversationId(UUID conversationId) {
        return find("conversationEntity.id = ?1 order by sentAt asc", conversationId).list();
    }
}
