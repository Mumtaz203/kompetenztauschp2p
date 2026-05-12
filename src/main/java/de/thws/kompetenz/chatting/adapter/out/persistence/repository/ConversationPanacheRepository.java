package de.thws.kompetenz.chatting.adapter.out.persistence.repository;

import de.thws.kompetenz.chatting.adapter.out.persistence.entity.ConversationEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@ApplicationScoped
public class ConversationPanacheRepository implements PanacheRepositoryBase<ConversationEntity,UUID> {
    public Optional<ConversationEntity> findConversationById(UUID conversationId) {
        return find("id",conversationId).firstResultOptional();
    }
    public List<ConversationEntity> findByUserId(UUID userId) {
        return find("user1Id = ?1 or user2Id = ?1 order by lastMessageAt desc",userId).list(); //i dont know anything about here its too complicated but i will learn
        //im not sure but i think it gets all of the conversations from user with checking if user took place like (a,b) or (b,a) and then order them by last chatting time
    }
    public Optional<ConversationEntity> findBetweenUsers(UUID user1Id, UUID user2Id) {
        return find("(user1Id = ?1 and user2Id = ?2) or (user1Id = ?2 and user2Id = ?1)", user1Id, user2Id)
                .firstResultOptional();
    }

}
