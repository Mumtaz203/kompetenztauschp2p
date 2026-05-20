package de.thws.kompetenz.auth.adapter.in.rest.dto;

public record LoginResponse (String token,
    String tokenType
) {
}