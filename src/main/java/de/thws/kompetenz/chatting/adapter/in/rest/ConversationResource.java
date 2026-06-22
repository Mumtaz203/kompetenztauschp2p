package de.thws.kompetenz.chatting.adapter.in.rest;

import de.thws.kompetenz.chatting.adapter.in.rest.dto.ConversationResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.CreateConversationRequest;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.MessageResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.dto.ShowConversationResponse;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.ConversationRestMapper;
import de.thws.kompetenz.chatting.adapter.in.rest.mapper.MessageRestMapper;
import de.thws.kompetenz.chatting.application.port.in.ConversationUseCaseI;
import de.thws.kompetenz.chatting.application.port.in.MessageUseCaseI;
import de.thws.kompetenz.chatting.domain.Conversation;
import de.thws.kompetenz.common.AuthorizationGuard;
import de.thws.kompetenz.user.application.port.out.UserRepositoryPort;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/conversations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ConversationResource {

    private final ConversationUseCaseI conversationUseCase;
    private final ConversationRestMapper conversationRestMapper;
    private final UserRepositoryPort userRepositoryPort;
    private final MessageUseCaseI messageUseCase;
    private final MessageRestMapper messageRestMapper;
    private final AuthorizationGuard authorizationGuard;

    public ConversationResource(
            ConversationUseCaseI conversationUseCase,
            ConversationRestMapper conversationRestMapper,
            UserRepositoryPort userRepositoryPort,
            MessageUseCaseI messageUseCase,
            MessageRestMapper messageRestMapper,
            AuthorizationGuard authorizationGuard
    ) {
        this.conversationUseCase = conversationUseCase;
        this.conversationRestMapper = conversationRestMapper;
        this.userRepositoryPort = userRepositoryPort;
        this.messageUseCase = messageUseCase;
        this.messageRestMapper = messageRestMapper;
        this.authorizationGuard = authorizationGuard;
    }

    @POST
    @RolesAllowed("USER")
    public Response createConversation(@Valid CreateConversationRequest request) {
        Conversation created = conversationUseCase.createConversation(
                authorizationGuard.currentUserId(),
                request.getOtherUserId()
        );

        ConversationResponse response = conversationRestMapper.toResponse(created);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @GET
    @Path("/{conversationId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getConversationById(@PathParam("conversationId") UUID conversationId) {
        Conversation conversation = conversationUseCase.getConversationById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation with id " + conversationId + " not found"));

        authorizationGuard.requireParticipantOrAdmin(
                conversation.hasParticipant(authorizationGuard.currentUserId())
        );

        ConversationResponse response = conversationRestMapper.toResponse(conversation);
        return Response.ok(response).build();
    }

    @GET
    @Path("/user/{userId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response getConversationsOfUser(@PathParam("userId") UUID userId) {
        authorizationGuard.requireSelfOrAdmin(userId);

        List<ConversationResponse> response = conversationUseCase.getConversationsOfUser(userId).stream()
                .map(conversationRestMapper::toResponse)
                .toList();

        return Response.ok(response).build();
    }

    @GET
    @Path("/between")
    @RolesAllowed({"USER", "ADMIN"})
    public Response findBetweenUsers(
            @QueryParam("user1Id") UUID user1Id,
            @QueryParam("user2Id") UUID user2Id
    ) {
        UUID currentUserId = authorizationGuard.currentUserId();

        authorizationGuard.requireParticipantOrAdmin(
                currentUserId.equals(user1Id) || currentUserId.equals(user2Id)
        );

        return conversationUseCase.findBetweenUsers(user1Id, user2Id)
                .map(conversationRestMapper::toResponse)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @DELETE
    @Path("/{conversationId}")
    @RolesAllowed({"USER", "ADMIN"})
    public Response deleteConversation(@PathParam("conversationId") UUID conversationId) {

        // there is a big mistake with this method we have to fix it eventually , so if one user deletes a conversation
        // it will be deleted from other participant too and noone would want that so we need to fix it like if he deletes the conversation
        // it has to be removed from visible conversationSecreen of the user but nor from databank ofc

        Conversation conversation = conversationUseCase.getConversationById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation with id " + conversationId + " not found"));

        authorizationGuard.requireParticipantOrAdmin(
                conversation.hasParticipant(authorizationGuard.currentUserId())
        );

        Conversation deleted = conversationUseCase.deleteConversation(conversationId);
        ConversationResponse response = conversationRestMapper.toResponse(deleted);

        return Response.ok(response).build();
    }

    @GET
    @Path("/{conversationId}/details")
    @RolesAllowed({"USER", "ADMIN"})
    public Response showConversationWithId(@PathParam("conversationId") UUID conversationId) {
        Conversation conversation = conversationUseCase.getConversationById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation with id " + conversationId + " not found"));

        authorizationGuard.requireParticipantOrAdmin(
                conversation.hasParticipant(authorizationGuard.currentUserId())
        );

        List<MessageResponse> messages = messageUseCase.getMessagesByConversationId(conversationId).stream()
                .map(messageRestMapper::toResponse)
                .toList();

        String user1Name = userRepositoryPort.findUserById(conversation.getUser1Id())
                .orElseThrow(() -> new NotFoundException("User not found: " + conversation.getUser1Id()))
                .getUsername();

        String user2Name = userRepositoryPort.findUserById(conversation.getUser2Id())
                .orElseThrow(() -> new NotFoundException("User not found: " + conversation.getUser2Id()))
                .getUsername();

        ShowConversationResponse response = conversationRestMapper.toShowConversationResponse(conversation);

        response.setUser1Name(user1Name);
        response.setUser2Name(user2Name);
        response.setMessages(messages);

        return Response.ok(response).build();
    }
}