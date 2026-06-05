package de.thws.kompetenz.session.adapter.in.rest.dto;

import java.util.UUID;


public record CreateSessionRequest (
        UUID matchingRequestId,
        UUID requesterUserId,
        UUID receiverUserId )
{
}
