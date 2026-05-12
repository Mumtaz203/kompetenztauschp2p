package de.thws.kompetenz.chatting.application.service;

import de.thws.kompetenz.chatting.application.port.in.ConversationUseCaseI;
import de.thws.kompetenz.chatting.application.port.out.ConversationRepositoryPortI;
import de.thws.kompetenz.chatting.domain.Conversation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Transactional
@ApplicationScoped
public class ConversationService implements ConversationUseCaseI {
    private final ConversationRepositoryPortI conversationRepository;

    public ConversationService(ConversationRepositoryPortI conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Conversation createConversation(UUID currentUserId, UUID otherUserId) {
        // Validate inputs
        if (currentUserId == null) {
            throw new IllegalArgumentException("Current user ID cannot be null");
        }
        if (otherUserId == null) {
            throw new IllegalArgumentException("Other user ID cannot be null");
        }
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot create conversation with yourself");
        }
        // Normalize user IDs: smaller UUID -> user1, larger -> user2
        // with this (A,B) ve (B,A) are same conversations
        //sunun olmasini sagliyor :
        // İstek 1: createConversation(A, B) -> (A,B)
        //İstek 2: createConversation(B, A) -> yine (A,B)
        UUID user1Id = currentUserId.compareTo(otherUserId) < 0 ? currentUserId : otherUserId;
        UUID user2Id = currentUserId.compareTo(otherUserId) < 0 ? otherUserId : currentUserId;

        Optional<Conversation> existingConversation = conversationRepository.findBetweenUsers(user1Id, user2Id);
        if (existingConversation.isPresent()) {
            return existingConversation.get();
        }
        //create new conversation
        Conversation conversation = new Conversation(user1Id, user2Id);
        return conversationRepository.save(conversation);


    }

    @Override
    public Optional<Conversation> getConversationById(UUID conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID cannot be null");
        }
        return conversationRepository.findById(conversationId);
    }

    @Override
    public List<Conversation> getConversationsOfUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return conversationRepository.findByUserId(userId);
    }

    @Override
    public Optional<Conversation> findBetweenUsers(UUID user1Id, UUID user2Id) {
        if (user1Id == null || user2Id == null) {
            throw new IllegalArgumentException("User IDs cannot be null");
        }
        if (user1Id.equals(user2Id)) {
            throw new IllegalArgumentException("Cannot find conversation between the same user");
        }
        // Normalize user IDs: smaller UUID -> user1, larger -> user2
        UUID normalizedUser1Id = user1Id.compareTo(user2Id) < 0 ? user1Id : user2Id;
        UUID normalizedUser2Id = user1Id.compareTo(user2Id) < 0 ? user2Id : user1Id;

        return conversationRepository.findBetweenUsers(normalizedUser1Id, normalizedUser2Id);
    }

    @Override
    public Conversation deleteConversation(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("No conversation found with id: " + conversationId));
        boolean deleted = conversationRepository.deleteById(conversationId);
        if (!deleted) {
            throw new IllegalStateException("Conversation could not be deleted: " + conversationId);
        }
        return conversation;
    }

}
