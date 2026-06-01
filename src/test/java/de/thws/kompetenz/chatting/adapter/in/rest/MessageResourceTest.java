package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Message;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@QuarkusTest
class MessageResourceTest {

    @InjectMock MessageUseCaseI messageUseCase;
    @InjectMock MessageRestMapper messageRestMapper;

    @InjectMock
    JsonWebToken jwt;          // Keep this

    @BeforeEach
    void setup() {
        // This is now safe
        Mockito.when(jwt.getSubject()).thenReturn(UUID.randomUUID().toString());
    }

    @Test
    void sendMessage_returns201() {
        UUID conversationId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        Message saved = new Message();
        saved.setId(messageId);
        saved.setConversationId(conversationId);
        saved.setSenderId(senderId);
        saved.setRecipientId(recipientId);
        saved.setContent("Selam");

        MessageResponse dto = new MessageResponse();
        dto.setId(messageId);
        dto.setConversationId(conversationId);
        dto.setSenderId(senderId);
        dto.setRecipientId(recipientId);
        dto.setContent("Selam");

        when(messageUseCase.sendMessage(conversationId, senderId, recipientId, "Selam")).thenReturn(saved);
        when(messageRestMapper.toResponse(saved)).thenReturn(dto);

        String body = """
                {
                  "conversationId":"%s",
                  "senderId":"%s",
                  "recipientId":"%s",
                  "content":"Selam"
                }
                """.formatted(conversationId, senderId, recipientId);

        given()
                .contentType("application/json")
                .body(body)
                .when().post("/messages")
                .then()
                .statusCode(201)
                .body("id", equalTo(messageId.toString()));
    }

    @Test
    void getMessageById_returns404_whenMissing() {
        UUID id = UUID.randomUUID();
        when(messageUseCase.getMessageById(id)).thenReturn(Optional.empty());

        given()
                .when().get("/messages/{id}", id)
                .then()
                .statusCode(404);
    }

    @Test
    void getMessageById_returns200_whenFound() {
        UUID id = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();

        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setContent("hello");

        MessageResponse response = new MessageResponse();
        response.setId(id);
        response.setConversationId(conversationId);
        response.setContent("hello");

        when(messageUseCase.getMessageById(id)).thenReturn(Optional.of(message));
        when(messageRestMapper.toResponse(message)).thenReturn(response);

        given()
                .when().get("/messages/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.toString()))
                .body("conversationId", equalTo(conversationId.toString()))
                .body("content", equalTo("hello"));
    }

    @Test
    void getByConversation_returnsList() {
        UUID conversationId = UUID.randomUUID();
        UUID messageId = UUID.randomUUID();

        Message m = new Message();
        m.setId(messageId);
        m.setConversationId(conversationId);
        m.setContent("test");

        MessageResponse dto = new MessageResponse();
        dto.setId(messageId);
        dto.setConversationId(conversationId);
        dto.setContent("test");

        when(messageUseCase.getMessagesByConversationId(conversationId)).thenReturn(List.of(m));
        when(messageRestMapper.toResponse(m)).thenReturn(dto);

        given()
                .when().get("/messages/conversation/{id}", conversationId)
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].id", equalTo(messageId.toString()));
    }

    @Test
    void markAsRead_returns204() {
        UUID messageId = UUID.randomUUID();

        given()
                .when().patch("/messages/{id}/read", messageId)
                .then()
                .statusCode(204);

        verify(messageUseCase).markMessageAsRead(messageId);
    }
}
