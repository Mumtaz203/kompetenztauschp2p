package de.thws.kompetenz.chatting.application.port.in;

import de.thws.kompetenz.chatting.domain.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationUseCaseI {
    Conversation createConversation(UUID currentUserId, UUID otherUserId);

    Optional<Conversation> getConversationById(UUID conversationId);

    List<Conversation> getConversationsOfUser(UUID userId);

    Optional<Conversation> findBetweenUsers(UUID user1Id, UUID user2Id);
    Conversation deleteConversation(UUID conversationId);



}
