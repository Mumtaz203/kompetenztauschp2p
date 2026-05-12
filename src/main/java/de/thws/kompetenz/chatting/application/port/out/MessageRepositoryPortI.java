package de.thws.kompetenz.chatting.application.port.out;

import de.thws.kompetenz.chatting.domain.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepositoryPortI {
    Message save(Message message);

    Optional<Message> findById(UUID messageId);

    List<Message> findByConversationId(UUID conversationId);

    void markAsRead(UUID messageId);
}
