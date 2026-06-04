package de.thws.kompetenz.chatting.adapter.out.persistence;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.ConversationEntity;
import de.thws.kompetenz.chatting.adapter.out.persistence.entity.MessageEntity;
import de.thws.kompetenz.chatting.adapter.out.persistence.mapper.MessagePersistenceMapper;
import de.thws.kompetenz.chatting.adapter.out.persistence.repository.ConversationPanacheRepository;
import de.thws.kompetenz.chatting.adapter.out.persistence.repository.MessagePanacheRepository;
import de.thws.kompetenz.chatting.application.port.out.MessageRepositoryPortI;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
@Transactional
public class MessagePersistenceAdapter implements MessageRepositoryPortI {
    private final MessagePanacheRepository messagePanacheRepository;
    private final ConversationPanacheRepository conversationPanacheRepository;
    private final MessagePersistenceMapper messagePersistenceMapper;

    public MessagePersistenceAdapter(MessagePanacheRepository messagePanacheRepository, ConversationPanacheRepository conversationPanacheRepository, MessagePersistenceMapper messagePersistenceMapper) {
        this.messagePanacheRepository = messagePanacheRepository;
        this.conversationPanacheRepository = conversationPanacheRepository;
        this.messagePersistenceMapper = messagePersistenceMapper;
    }

    @Override
    public Message save(Message message) {
        if(message==null){
            throw new IllegalArgumentException("Message connot be null");
        }
        if (message.getConversationId()==null){
            throw new IllegalArgumentException("ConversationId connot be null");
        }
        ConversationEntity conversationEntity = conversationPanacheRepository.findConversationById(message.getConversationId())
                .orElseThrow(()->new IllegalArgumentException("ConversationId not found"));
        if(message.getId()==null ||messagePanacheRepository.findMessageById(message.getId()).isEmpty()){


            //we need to create new MessageEntity and persist it because we need to set the conversationEntity which is required for MessageEntity

            //we could create it with mappers but we need to set the conversationEntity which is required for MessageEntity
            // and we can not set it with mappers because we only have the conversationId in Message domain model
            MessageEntity toCreate= new MessageEntity();

            toCreate.setId(message.getId());
            toCreate.setContent(message.getContent());
            toCreate.setRead(message.isRead());
            toCreate.setSenderId(message.getSenderId());
            toCreate.setRecipientId(message.getRecipientId());
            toCreate.setConversationEntity(conversationEntity); //here u can check
            toCreate.setSentAt(message.getSentAt());

            messagePanacheRepository.persist(toCreate);
            return messagePersistenceMapper.toDomain(toCreate);
        }
        MessageEntity existing=messagePanacheRepository.findMessageById(message.getId()).get();
        existing.setContent(message.getContent());
        existing.setRead(message.isRead());
        existing.setSenderId(message.getSenderId());
        existing.setRecipientId(message.getRecipientId());
        existing.setConversationEntity(conversationEntity);
        existing.setSentAt(message.getSentAt());
        return  messagePersistenceMapper.toDomain(existing);


    }

    @Override
    public Optional<Message> findById(UUID messageId) {
       return messagePanacheRepository
               .findMessageById(messageId)//Optional<MessageEntity>
               .map(message->messagePersistenceMapper.toDomain(message));//Optional<Message>
    }

    @Override
    public List<Message> findByConversationId(UUID conversationId) {
        return messagePanacheRepository.findByConversationId(conversationId)
                .stream().map(messageEntity->messagePersistenceMapper.toDomain(messageEntity)).toList();
    }

    @Override
    public void markAsRead(UUID messageId) {
        MessageEntity messageEntity=messagePanacheRepository.findMessageById(messageId)
                .orElseThrow(()->new IllegalArgumentException("Message not found"));
        messageEntity.setRead(true);

    }

    @Override
    public boolean deleteMessageById(UUID messageId) {
        if(messageId==null){
            throw new IllegalArgumentException("MessageId connot be null");
        }
       return messagePanacheRepository.deleteById(messageId);
    }

    @Override
    public List<Message> getAllMesagesByUserId(UUID userId) {
        if (userId==null){
            throw new IllegalArgumentException("UserId connot be null");
        }
        return messagePanacheRepository.getAllMessagesByUserId(userId)
                .stream().map(messageEntity->messagePersistenceMapper.toDomain(messageEntity)).toList();
    }


}
