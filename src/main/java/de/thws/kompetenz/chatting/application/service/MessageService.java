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

    @Override
    public Message deleteMessage(UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        boolean deleted = messageRepository.deleteMessageById(messageId);
        if (!deleted) {
            throw new IllegalArgumentException("Message with ID " + messageId + " could not be deleted");
        }
        return message;
    }

    @Override
    public Message updateMessage(UUID messageId, Message message) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        Message existing = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + messageId));

        // Keep runtime-controlled fields stable unless explicitly provided.
        if (message.getConversationId() == null) {
            message.setConversationId(existing.getConversationId());
        }
        if (message.getSenderId() == null) {
            message.setSenderId(existing.getSenderId());
        }
        if (message.getRecipientId() == null) {
            message.setRecipientId(existing.getRecipientId());
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            message.setContent(existing.getContent());
        } else {
            message.setContent(message.getContent().trim());
        }

        message.setId(messageId);
        message.setSentAt(existing.getSentAt());
        message.setRead(existing.isRead());
        // we set this attiruts here becase in save method we need sentAt Read end Id values but since Update Messsage Request does not have them
        //i hade to set them here or change the Request inhalt , but i didint wanted to do that , necessery infromations can be setted form here :)

        return messageRepository.save(message);
    }

    @Override
    public List<Message> getAllMessagesFromUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return messageRepository.getAllMesagesByUserId(userId);
    }
}
