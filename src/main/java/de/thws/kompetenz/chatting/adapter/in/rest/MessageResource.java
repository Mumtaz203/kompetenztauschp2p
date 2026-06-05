package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.CreateMessageRequest;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Message;
import jakarta.annotation.security.RolesAllowed;
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
    @RolesAllowed("USER")
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
    @RolesAllowed({"USER","ADMIN"})
    public Response getMessageById(@PathParam("messageId") UUID messageId) {
        return messageUseCase.getMessageById(messageId)
                .map(messageRestMapper::toResponse)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @GET
    @Path("/conversation/{conversationId}")
    @RolesAllowed({"USER","ADMIN"})
    public Response getMessagesByConversationId(@PathParam("conversationId") UUID conversationId) {
        List<MessageResponse> response = messageUseCase.getMessagesByConversationId(conversationId).stream()
                .map(messageRestMapper::toResponse)
                .toList();
        return Response.ok(response).build();
    }

    @PATCH
    @Path("/{messageId}/read")
    @RolesAllowed("USER")
    public Response markAsRead(@PathParam("messageId") UUID messageId) {
        messageUseCase.markMessageAsRead(messageId);
        return Response.noContent().build();
    }

    @GET
    @Path("/messagesFromUser/{userId}")
    @RolesAllowed("ADMIN")
    public Response getAllMessagesFromUser(@PathParam("userId") UUID userId) {
        List<MessageResponse> response = messageUseCase.getAllMessagesFromUser(userId).stream()
                .map(messageRestMapper::toResponse)
                .toList();
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/deleteMessage/{messageId}")
    @RolesAllowed("ADMIN")
    public Response deleteMessageById(@PathParam("messageId") UUID messageId) {
        Message message=messageUseCase.deleteMessage(messageId);
        MessageResponse response = messageRestMapper.toResponse(message);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/updateMessage/{messageId}")
    @RolesAllowed("ADMIN")
    public Response updateMessage(@PathParam("messageId") UUID messageId,@Valid CreateMessageRequest request) {//we can have a different request class for update if we want to allow only some fields to be updated
       Message message= messageRestMapper.toModel(request);
       //id is setted here because our createMessageRequest does not have id attribute because it does not need it (other attibuts are setted in mapper class)
        // and i didint wanted to create a updateUserRequest because it would be almost the same as createUserRequest (this solution might be false)
        Message updated = messageUseCase.updateMessage(messageId,message);
        MessageResponse response = messageRestMapper.toResponse(updated);
        return Response.ok(response).build();
    }









}
