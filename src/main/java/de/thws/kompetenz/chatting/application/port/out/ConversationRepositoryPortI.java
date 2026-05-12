package de.thws.kompetenz.chatting.application.port.out;

import de.thws.kompetenz.chatting.domain.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepositoryPortI {
    Conversation save(Conversation conversation);

    Optional<Conversation> findById(UUID conversationId);

    List<Conversation> findByUserId(UUID userId);

    Optional<Conversation> findBetweenUsers(UUID user1Id, UUID user2Id);

    boolean deleteById(UUID conversationId);
}

