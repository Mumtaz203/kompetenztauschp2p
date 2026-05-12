package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.CreateMessageRequest;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/messages")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class MessageResource {

    private final MessageUseCaseI messageUseCase;
    private final MessageRestMapper messageRestMapper;

    public MessageResource(MessageUseCaseI messageUseCase, MessageRestMapper messageRestMapper) {
        this.messageUseCase = messageUseCase;
        this.messageRestMapper = messageRestMapper;
    }

    @POST
    public Response sendMessage(@Valid CreateMessageRequest request) {
        Message created = messageUseCase.sendMessage(
                request.getConversationId(),
                request.getSenderId(),
                request.getRecipientId(),
                request.getContent()
        );

        MessageResponse response = messageRestMapper.toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{messageId}")
    public Response getMessageById(@PathParam("messageId") UUID messageId) {
        return messageUseCase.getMessageById(messageId)
                .map(messageRestMapper::toResponse)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/conversation/{conversationId}")
    public Response getMessagesByConversationId(@PathParam("conversationId") UUID conversationId) {
        List<MessageResponse> response = messageUseCase.getMessagesByConversationId(conversationId).stream()
                .map(messageRestMapper::toResponse)
                .toList();
        return Response.ok(response).build();
    }

    @PATCH
    @Path("/{messageId}/read")
    public Response markAsRead(@PathParam("messageId") UUID messageId) {
        messageUseCase.markMessageAsRead(messageId);
        return Response.noContent().build();
    }
}
