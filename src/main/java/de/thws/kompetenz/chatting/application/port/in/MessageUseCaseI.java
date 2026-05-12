package de.thws.kompetenz.chatting.application.port.in;

import de.thws.kompetenz.chatting.domain.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageUseCaseI {
    Message sendMessage(UUID conversationId, UUID senderId, UUID recipientId, String content);

    List<Message> getMessagesByConversationId(UUID conversationId);

    Optional<Message> getMessageById(UUID messageId);

    void markMessageAsRead(UUID messageId);
}
