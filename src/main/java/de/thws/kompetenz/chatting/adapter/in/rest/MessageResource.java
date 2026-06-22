package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.CreateMessageRequest;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.ConversationUseCaseI;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Conversation;
import de.thws.kompetenz.chatting.domain.Message;
import de.thws.kompetenz.common.AuthorizationGuard;
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
    private final ConversationUseCaseI conversationUseCase;
    private final AuthorizationGuard authorizationGuard;
    private final MessageRestMapper messageRestMapper;

    public MessageResource(MessageUseCaseI messageUseCase, ConversationUseCaseI conversationUseCase, AuthorizationGuard authorizationGuard, MessageRestMapper messageRestMapper) {
        this.messageUseCase = messageUseCase;
        this.conversationUseCase = conversationUseCase;
        this.authorizationGuard = authorizationGuard;
        this.messageRestMapper = messageRestMapper;
    }

    @POST
    @RolesAllowed("USER")
    public Response sendMessage(@Valid CreateMessageRequest request) {

        UUID currentUserId = authorizationGuard.currentUserId();
        //check if the sender is the current user



        // u can think how does this function work, first of all user has
        // to give a token and then in guard system we translate
        // this token to UUID with this method and then here we check
        // if the senderId is the same as the userId


        Conversation conversation = conversationUseCase.getConversationById(request.getConversationId())
                .orElseThrow(() -> new NotFoundException("Conversation not found" + request.getConversationId()));

        authorizationGuard.requireSelfOrAdmin(request.getSenderId());

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

        Message message = messageUseCase.getMessageById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));

        UUID currentUserId = authorizationGuard.currentUserId();
        // we need to check if the user is participant of the conversation or admin, if not

        boolean participant = currentUserId.equals(message.getSenderId())
                || currentUserId.equals(message.getRecipientId());

        authorizationGuard.requireParticipantOrAdmin(participant);


        MessageResponse response = messageRestMapper.toResponse(message);
        return Response.ok(response).build();
    }

    @GET
    @Path("/conversation/{conversationId}")
    @RolesAllowed({"USER","ADMIN"})
    public Response getMessagesByConversationId(@PathParam("conversationId") UUID conversationId) {
        Conversation conversation = conversationUseCase.getConversationById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found with id: " + conversationId));

        authorizationGuard.requireParticipantOrAdmin(conversation.hasParticipant(authorizationGuard.currentUserId()));
        List<MessageResponse> response = messageUseCase.getMessagesByConversationId(conversationId).stream()
                .map(messageRestMapper::toResponse)
                .toList();
        return Response.ok(response).build();
    }

    @PATCH
    @Path("/{messageId}/read")
    @RolesAllowed("USER")
    public Response markAsRead(@PathParam("messageId") UUID messageId) {
        Message message = messageUseCase.getMessageById(messageId)
                .orElseThrow(() -> new NotFoundException("Message not found with id: " + messageId));

        UUID currentUserId = authorizationGuard.currentUserId();

        if (!currentUserId.equals(message.getRecipientId())) {
            throw new ForbiddenException("Only the recipient can mark this message as read.");
        }


        messageUseCase.markMessageAsRead(messageId);
        return Response.noContent().build();
    }

    @GET
    @Path("/messagesFromUser/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getAllMessagesFromUser(@PathParam("userId") UUID userId) {

        authorizationGuard.requireSelfOrAdmin(userId);

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
    public Response updateMessage(@PathParam("messageId") UUID messageId,@Valid CreateMessageRequest request) {
        authorizationGuard.requireSelfOrAdmin(request.getSenderId());
        //we can have a different request class for update if we want to allow only some fields to be updated
        Message message= messageRestMapper.toModel(request);
        //id is setted here because our createMessageRequest does not have id attribute because it does not need it (other attibuts are setted in mapper class)
        // and i didint wanted to create a updateUserRequest because it would be almost the same as createUserRequest (this solution might be false)

        Message updated = messageUseCase.updateMessage(messageId,message);
        MessageResponse response = messageRestMapper.toResponse(updated);
        return Response.ok(response).build();
    }


}

