package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.ConversationResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.ConversationRestMapper;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.ConversationUseCaseI;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Conversation;
import de.thws.kompetenz.chatting.domain.Message;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import de.thws.kompetenz.user.domain.model.User;
import io.restassured.internal.http.HttpResponseException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@QuarkusTest
class ConversationResourceTest {

    @InjectMock ConversationUseCaseI conversationUseCase;
    @InjectMock ConversationRestMapper conversationRestMapper;
    @InjectMock UserRepositoryPort userRepositoryPort;
    @InjectMock MessageUseCaseI messageUseCase;
    @InjectMock MessageRestMapper messageRestMapper;

    @Test
    void createConversation_returns200_withCreatedEntityPayload() {
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        Conversation created = new Conversation();
        created.setId(conversationId);

        ConversationResponse response = new ConversationResponse();
        response.setId(conversationId);
        response.setUser1Id(currentUserId);
        response.setUser2Id(otherUserId);

        when(conversationUseCase.createConversation(currentUserId, otherUserId)).thenReturn(created);
        when(conversationRestMapper.toResponse(created)).thenReturn(response);

        String body = """
                {
                  "currentUserId":"%s",
                  "otherUserId":"%s"
                }
                """.formatted(currentUserId, otherUserId);

        given()
                .contentType("application/json")
                .body(body)
                .when().post("/conversations")
                .then()
                .statusCode(200)
                .body("id", equalTo(conversationId.toString()))
                .body("user1Id", equalTo(currentUserId.toString()))
                .body("user2Id", equalTo(otherUserId.toString()));
    }

    @Test
    void getConversationById_returns200() {
        UUID id = UUID.randomUUID();
        Conversation domain = new Conversation();
        domain.setId(id);

        ConversationResponse dto = new ConversationResponse();
        dto.setId(id);

        when(conversationUseCase.getConversationById(id)).thenReturn(Optional.of(domain));
        when(conversationRestMapper.toResponse(domain)).thenReturn(dto);

        given()
                .when().get("/conversations/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.toString()));
    }

    @Test
    void getConversationById_returns404_whenMissing() {
        UUID id = UUID.randomUUID();
        when(conversationUseCase.getConversationById(id)).thenReturn(Optional.empty());

        HttpResponseException ex = assertThrows(HttpResponseException.class,
                () -> given().when().get("/conversations/{id}", id));
        assertTrue(ex.getMessage().contains("status code: 404"));
    }

    @Test
    void getConversationsOfUser_returns200_withList() {
        UUID userId = UUID.randomUUID();
        UUID conv1 = UUID.randomUUID();
        UUID conv2 = UUID.randomUUID();

        Conversation c1 = new Conversation();
        c1.setId(conv1);
        Conversation c2 = new Conversation();
        c2.setId(conv2);

        ConversationResponse r1 = new ConversationResponse();
        r1.setId(conv1);
        ConversationResponse r2 = new ConversationResponse();
        r2.setId(conv2);

        when(conversationUseCase.getConversationsOfUser(userId)).thenReturn(List.of(c1, c2));
        when(conversationRestMapper.toResponse(c1)).thenReturn(r1);
        when(conversationRestMapper.toResponse(c2)).thenReturn(r2);

        given()
                .when().get("/conversations/user/{userId}", userId)
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("id", hasItems(conv1.toString(), conv2.toString()));
    }

    @Test
    void findBetweenUsers_returns200_whenFound() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);

        ConversationResponse response = new ConversationResponse();
        response.setId(conversationId);

        when(conversationUseCase.findBetweenUsers(user1, user2)).thenReturn(Optional.of(conversation));
        when(conversationRestMapper.toResponse(conversation)).thenReturn(response);

        given()
                .queryParam("user1Id", user1)
                .queryParam("user2Id", user2)
                .when().get("/conversations/between")
                .then()
                .statusCode(200)
                .body("id", equalTo(conversationId.toString()));
    }

    @Test
    void findBetweenUsers_returns404_whenMissing() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        when(conversationUseCase.findBetweenUsers(user1, user2)).thenReturn(Optional.empty());

        HttpResponseException ex = assertThrows(HttpResponseException.class,
                () -> given()
                        .queryParam("user1Id", user1)
                        .queryParam("user2Id", user2)
                        .when().get("/conversations/between"));
        assertTrue(ex.getMessage().contains("status code: 404"));
    }

    @Test
    void deleteConversation_returns200_withDeletedEntity() {
        UUID conversationId = UUID.randomUUID();

        Conversation deleted = new Conversation();
        deleted.setId(conversationId);

        ConversationResponse response = new ConversationResponse();
        response.setId(conversationId);

        when(conversationUseCase.deleteConversation(conversationId)).thenReturn(deleted);
        when(conversationRestMapper.toResponse(deleted)).thenReturn(response);

        given()
                .when().delete("/conversations/{id}", conversationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(conversationId.toString()));
    }

    @Test
    void details_returnsConversationWithMessagesAndUsernames() {
        UUID conversationId = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        Conversation conversation = new Conversation();
        conversation.setId(conversationId);
        conversation.setUser1Id(user1);
        conversation.setUser2Id(user2);
        conversation.setCreatedAt(LocalDateTime.now().minusDays(1));
        conversation.setLastMessageAt(LocalDateTime.now());

        Message message = new Message();
        message.setId(messageId);
        message.setConversationId(conversationId);
        message.setSenderId(user1);
        message.setRecipientId(user2);
        message.setContent("hi");

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(messageId);
        messageResponse.setConversationId(conversationId);
        messageResponse.setSenderId(user1);
        messageResponse.setRecipientId(user2);
        messageResponse.setContent("hi");

        User u1 = new User(user1, "samet", "samet@mail.com", "x");
        User u2 = new User(user2, "mumtaz", "mumtaz@mail.com", "x");

        when(conversationUseCase.getConversationById(conversationId)).thenReturn(Optional.of(conversation));
        when(messageUseCase.getMessagesByConversationId(conversationId)).thenReturn(List.of(message));
        when(messageRestMapper.toResponse(message)).thenReturn(messageResponse);
        when(userRepositoryPort.findUserById(user1)).thenReturn(Optional.of(u1));
        when(userRepositoryPort.findUserById(user2)).thenReturn(Optional.of(u2));

        given()
                .when().get("/conversations/{id}/details", conversationId)
                .then()
                .statusCode(200)
                .body("conversationId", equalTo(conversationId.toString()))
                .body("user1Name", equalTo("samet"))
                .body("user2Name", equalTo("mumtaz"))
                .body("messages", hasSize(1));
    }

    @Test
    void details_returns404_whenConversationMissing() {
        UUID conversationId = UUID.randomUUID();
        when(conversationUseCase.getConversationById(conversationId)).thenReturn(Optional.empty());

        HttpResponseException ex = assertThrows(HttpResponseException.class,
                () -> given().when().get("/conversations/{id}/details", conversationId));
        assertTrue(ex.getMessage().contains("status code: 404"));
    }
}
