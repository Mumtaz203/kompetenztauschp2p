package de.thws.kompetenz.chatting.adapter.out.persistence;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.ConversationEntity;
import de.thws.kompetenz.chatting.adapter.out.persistence.mapper.ConversationPersistenceMapper;
import de.thws.kompetenz.chatting.adapter.out.persistence.repository.ConversationPanacheRepository;
import de.thws.kompetenz.chatting.application.port.out.ConversationRepositoryPortI;
import de.thws.kompetenz.chatting.domain.Conversation;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped

public class ConversationPersistenceAdapter implements ConversationRepositoryPortI {

    private final ConversationPanacheRepository conversationPanacheRepository;
    private final ConversationPersistenceMapper conversationPersistenceMapper;

    public ConversationPersistenceAdapter(ConversationPanacheRepository conversationPanacheRepository,
            ConversationPersistenceMapper conversationPersistenceMapper) {
        this.conversationPanacheRepository = conversationPanacheRepository;
        this.conversationPersistenceMapper = conversationPersistenceMapper;
    }


    @Override
    public Conversation save(Conversation conversation) {
        if(conversation == null) {
            throw new IllegalArgumentException("Conversation cannot be null");
        }
        // CREATE: if id is null, we create a new conversation
        if(conversation.getId()==null) {
            ConversationEntity toCreate = conversationPersistenceMapper.toEntity(conversation);
            conversationPanacheRepository.persist(toCreate);
            return conversationPersistenceMapper.toDomain(toCreate);
        }
        // UPDATE: if id is not null, we check if the conversation exists, if it does we update it, if not we throw an exception
       ConversationEntity existing =conversationPanacheRepository.findConversationById(conversation.getId())
               .orElseThrow(()->new IllegalArgumentException("Conversation with id "+conversation.getId()+" not found"));
        existing.setUser1Id(conversation.getUser1Id());
        existing.setUser2Id(conversation.getUser2Id());
        existing.setLastMessageAt(conversation.getLastMessageAt());

        return conversationPersistenceMapper.toDomain(existing);

    }

    @Override
    public Optional<Conversation> findById(UUID conversationId) {
        return conversationPanacheRepository.findConversationById(conversationId)
                .map(conversation->conversationPersistenceMapper.toDomain(conversation));
    }

    @Override
    public List<Conversation> findByUserId(UUID userId) {
        return conversationPanacheRepository.findByUserId(userId).stream()
                .map(conversationEntity ->
                        conversationPersistenceMapper.toDomain(conversationEntity))
                .toList();
    }

    @Override
    public Optional<Conversation> findBetweenUsers(UUID user1Id, UUID user2Id) {
        return conversationPanacheRepository.findBetweenUsers(user1Id,user2Id)
                .map(conversationEntity ->
                        conversationPersistenceMapper.toDomain(conversationEntity));
    }

    @Override
    public boolean deleteById(UUID conversationId) {
        if(conversationId == null) {
            throw new IllegalArgumentException("Conversation cannot be null");
        }
        return conversationPanacheRepository.deleteById(conversationId);
    }
}
