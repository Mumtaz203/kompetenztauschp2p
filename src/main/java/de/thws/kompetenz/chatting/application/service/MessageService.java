package de.thws.kompetenz.chatting.application.service;

import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.application.port.out.ConversationRepositoryPortI;
import de.thws.kompetenz.chatting.application.port.out.MessageRepositoryPortI;
import de.thws.kompetenz.chatting.domain.Conversation;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Transactional
public class MessageService implements MessageUseCaseI {
    private final MessageRepositoryPortI messageRepository;
    private final ConversationRepositoryPortI conversationRepository;

    public MessageService(MessageRepositoryPortI messageRepository, ConversationRepositoryPortI conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Message sendMessage(UUID conversationId, UUID senderId, UUID recipientId, String content) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID cannot be null");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("Sender ID cannot be null");
        }
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID cannot be null");
        }
        if (senderId.equals(recipientId)) {
            throw new IllegalArgumentException("Sender and recipient cannot be the same user");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        if (!conversation.hasParticipant(senderId) || !conversation.hasParticipant(recipientId)) {
            throw new IllegalArgumentException("Sender and recipient must be participants of the conversation");
        }

        UUID otherParticipant = conversation.getOtherParticipant(senderId);
        if (!recipientId.equals(otherParticipant)) {
            throw new IllegalArgumentException("Recipient must be the other participant of the conversation");
        }

        Message message = new Message(conversationId, senderId, recipientId, content.trim());
        Message savedMessage = messageRepository.save(message);

        conversation.touchLastMessageAt();
        conversationRepository.save(conversation);

        return savedMessage;
    }

    @Override
    public List<Message> getMessagesByConversationId(UUID conversationId) {
        if (conversationId == null) {
            throw new IllegalArgumentException("Conversation ID cannot be null");
        }
        return messageRepository.findByConversationId(conversationId);
    }

    @Override
    public Optional<Message> getMessageById(UUID messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
        return messageRepository.findById(messageId);
    }

    @Override
    public void markMessageAsRead(UUID messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
        messageRepository.markAsRead(messageId);
    }
}
