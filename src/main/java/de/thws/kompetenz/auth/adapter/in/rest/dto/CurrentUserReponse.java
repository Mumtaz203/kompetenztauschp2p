package de.thws.kompetenz.auth.adapter.in.rest.dto;

import java.util.UUID;

public record CurrentUserReponse(
        UUID id,
        String username,
        String email
) {
}
